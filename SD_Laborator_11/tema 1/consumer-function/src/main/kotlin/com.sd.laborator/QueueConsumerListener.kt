package com.sd.laborator

import io.micronaut.rabbitmq.annotation.Queue
import io.micronaut.rabbitmq.annotation.RabbitListener
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Listener RabbitMQ care asculta coada "numbers-queue" si acumuleaza
 * numerele primite in NumbersAccumulator.
 *
 * Fiecare mesaj din coada contine un singur numar intreg (publicat de
 * serviciul producator care citeste din fisier).
 */
@RabbitListener
class QueueConsumerListener {

    @Inject
    private lateinit var numbersAccumulator: NumbersAccumulator

    private val LOG: Logger = LoggerFactory.getLogger(QueueConsumerListener::class.java)

    /**
     * Metoda apelata automat de Micronaut pentru fiecare mesaj din coada.
     * @param number numarul intreg primit din coada
     */
    @Queue("numbers-queue")
    fun receiveNumber(number: Int) {
        LOG.info("Mesaj primit din coada RabbitMQ: $number")
        numbersAccumulator.addNumber(number)
    }
}