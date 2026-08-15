package com.sd.laborator

import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Singleton care acumuleaza numerele primite din coada RabbitMQ.
 *
 * Deoarece mesajele sosesc individual (cate un numar pe mesaj),
 * acest acumulator pastreaza lista curenta de numere pana cand
 * functia serverless este invocata pentru a le procesa.
 *
 * Thread-safe prin CopyOnWriteArrayList.
 */
@Singleton
class NumbersAccumulator {

    private val LOG: Logger = LoggerFactory.getLogger(NumbersAccumulator::class.java)

    // Lista thread-safe cu numerele acumulate din coada
    private val accumulatedNumbers = CopyOnWriteArrayList<Int>()

    /**
     * Adauga un numar in lista acumulata.
     */
    fun addNumber(number: Int) {
        accumulatedNumbers.add(number)
        LOG.debug("Numarul $number adaugat in acumulator. Total acumulat: ${accumulatedNumbers.size}")
    }

    /**
     * Returneaza snapshot-ul curent al listei acumulate si o goleste.
     * Operatie atomica: preia si reseteaza.
     */
    @Synchronized
    fun drainNumbers(): List<Int> {
        val snapshot = accumulatedNumbers.toList()
        accumulatedNumbers.clear()
        LOG.info("S-au preluat ${snapshot.size} numere din acumulator (acumulator resetat)")
        return snapshot
    }

    /**
     * Returneaza numarul de elemente acumulate (fara a le sterge).
     */
    fun size(): Int = accumulatedNumbers.size
}