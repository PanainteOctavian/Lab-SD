package com.sd.laborator

import io.micronaut.rabbitmq.annotation.Binding
import io.micronaut.rabbitmq.annotation.RabbitClient

@RabbitClient // exchangeu standard
interface NumbersProducer {

    @Binding("numbers-queue")
    fun sendNumber(number: Int)
}