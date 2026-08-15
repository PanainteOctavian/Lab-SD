package com.sd.laborator

import io.micronaut.function.FunctionBean
import io.micronaut.function.executor.FunctionInitializer
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.function.Function

/**
 * Functia serverless consumer care:
 *  1. Preia numerele acumulate din coada RabbitMQ (via NumbersAccumulator)
 *  2. Foloseste ciurul lui Eratostene pentru a decide care sunt prime
 *  3. Returneaza DOAR numerele prime din lista primita (nu toata lista calculata)
 *
 * Primeste optional un maxNumber prin QueueRequest; daca lipseste, foloseste 1_000_000.
 * Parametrul "numbersToCheck" din QueueRequest este ignorat daca acumulatorul are date -
 * datele reale vin din coada. Poate fi folosit totusi pentru testare directa (fara coada).
 */
@FunctionBean("queue-consumer")
class QueueConsumerFunction : FunctionInitializer(), Function<QueueRequest, QueueResponse> {

    @Inject
    private lateinit var numbersAccumulator: NumbersAccumulator

    @Inject
    private lateinit var eratosteneSieveService: EratosteneSieveService

    private val LOG: Logger = LoggerFactory.getLogger(QueueConsumerFunction::class.java)

    override fun apply(request: QueueRequest): QueueResponse {
        val response = QueueResponse()

        // Preia numerele din acumulatorul cozii (drain atomic)
        val numbersFromQueue = numbersAccumulator.drainNumbers()

        // Daca acumulatorul era gol, foloseste lista din request (util pentru testare)
        val numbersToCheck = if (numbersFromQueue.isNotEmpty()) {
            LOG.info("Se proceseaza ${numbersFromQueue.size} numere preluate din coada RabbitMQ")
            numbersFromQueue
        } else {
            LOG.info("Coada RabbitMQ era goala; se proceseaza ${request.getNumbersToCheck().size} numere din request")
            request.getNumbersToCheck()
        }

        if (numbersToCheck.isEmpty()) {
            response.setMessage("Nu exista numere de procesat (nici in coada, nici in cerere)")
            response.setPrimeNumbers(emptyList())
            response.setNonPrimeNumbers(emptyList())
            response.setTotalChecked(0)
            return response
        }

        val maxNumber = request.getMaxNumber()

        // Valideaza ca maxNumber e in limitele serviciului Eratostene
        if (maxNumber >= eratosteneSieveService.MAX_SIZE) {
            LOG.error("maxNumber=$maxNumber depaseste limita ${eratosteneSieveService.MAX_SIZE}")
            response.setMessage("Parametrul maxNumber ($maxNumber) depaseste limita maxima " +
                    "${eratosteneSieveService.MAX_SIZE}")
            return response
        }

        LOG.info("Se calculeaza numerele prime pana la $maxNumber folosind ciurul lui Eratostene...")

        // Calculeaza TOATE numerele prime pana la maxNumber
        val allPrimesUpToMax = eratosteneSieveService.findPrimesLessThan(maxNumber)
        // Convertim la Set pentru lookup O(1)
        val primesSet = allPrimesUpToMax.toHashSet()

        LOG.info("Ciurul a calculat ${allPrimesUpToMax.size} numere prime pana la $maxNumber")
        LOG.info("Se filtreaza lista de ${numbersToCheck.size} numere primite...")

        // Imparte numerele din coada in prime si ne-prime
        val primes = mutableListOf<Int>()
        val nonPrimes = mutableListOf<Int>()

        for (n in numbersToCheck) {
            if (n < 2) {
                // 0 si 1 nu sunt prime prin definitie
                nonPrimes.add(n)
                LOG.debug("$n -> NU este prim (< 2)")
            } else if (n >= maxNumber) {
                // Numarul e in afara intervalului calculat; il tratam ca ne-prim
                // si logam un avertisment
                nonPrimes.add(n)
                LOG.warn("$n >= maxNumber ($maxNumber): in afara intervalului calculat, tratat ca ne-prim")
            } else if (primesSet.contains(n)) {
                primes.add(n)
                LOG.debug("$n -> PRIM")
            } else {
                nonPrimes.add(n)
                LOG.debug("$n -> NU este prim")
            }
        }

        response.setPrimeNumbers(primes)
        response.setNonPrimeNumbers(nonPrimes)
        response.setTotalChecked(numbersToCheck.size)
        response.setMessage(
            "Calcul efectuat cu succes! Din ${numbersToCheck.size} numere verificate, " +
                    "${primes.size} sunt prime si ${nonPrimes.size} nu sunt prime."
        )

        LOG.info("Rezultat: prime=${primes}, ne-prime=${nonPrimes}")
        return response
    }
}

/**
 * Punct de intrare CLI: echo '{"maxNumber":100}' | java -jar function.jar
 */
fun main(args: Array<String>) {
    val function = QueueConsumerFunction()
    function.run(args) { context -> function.apply(context.get(QueueRequest::class.java)) }
}