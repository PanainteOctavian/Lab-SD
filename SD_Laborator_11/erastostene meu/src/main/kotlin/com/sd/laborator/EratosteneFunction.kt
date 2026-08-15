package com.sd.laborator

import io.micronaut.function.FunctionBean
import io.micronaut.function.executor.FunctionInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.function.Function
import jakarta.inject.Inject

@FunctionBean("eratostene")
class EratosteneFunction : FunctionInitializer(),
    Function<EratosteneRequest, EratosteneResponse> {

    @Inject
    lateinit var eratosteneSieveService: EratosteneSieveService

    private val LOG: Logger = LoggerFactory.getLogger(EratosteneFunction::class.java)

    override fun apply(msg: EratosteneRequest): EratosteneResponse {
        val number = msg.getNumber()
        val response = EratosteneResponse()

        // Verificare limita
        if (number >= eratosteneSieveService.MAX_SIZE) {
            LOG.error("Parametru prea mare! $number > maximul de ${eratosteneSieveService.MAX_SIZE}")
            response.setMessage("Se accepta doar parametri mai mici ca " + eratosteneSieveService.MAX_SIZE)
            return response
        }

        LOG.info("Se calculeaza numerele prime pana la $number...")

        // Calculul si setarea raspunsului
        response.setPrimes(eratosteneSieveService.findPrimesLessThan(number))
        response.setMessage("Calcul efectuat cu succes!")

        LOG.info("Calcul incheiat!")
        return response
    }
}

// Punctul de intrare pentru CLI
fun main(args: Array<String>) {
    val function = EratosteneFunction()
    function.run(args) { context ->
        function.apply(context.get(EratosteneRequest::class.java))
    }
}