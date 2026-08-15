package com.sd.laborator.components

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sd.laborator.interfaces.PrimeNumberGenerator
import com.sd.laborator.model.CartesianPayload
import com.sd.laborator.model.Stack
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class StackAppComponent {
    private var A: Stack? = null
    private var B: Stack? = null

    @Autowired
    private lateinit var primeGenerator: PrimeNumberGenerator
    @Autowired
    private lateinit var connectionFactory: RabbitMqConnectionFactoryComponent

    private lateinit var amqpTemplate: AmqpTemplate
    private val mapper = jacksonObjectMapper()

    @Autowired
    fun initTemplate() {
        this.amqpTemplate = connectionFactory.rabbitTemplate()
    }

    @RabbitListener(queues = ["\${stackapp.rabbitmq.queue}"])
    fun recieveMessage(msg: String) {
        val processed_msg = (msg.split(",").map { it.toInt().toChar() }).joinToString(separator="")
        var result: String? = when(processed_msg) {
            "compute" -> computeExpression()
            "regenerate_A" -> regenerateA()
            "regenerate_B" -> regenerateB()
            else -> null
        }

        // Dacă e null (cum e la compute acum), nu trimite nimic.
        // Va trimite UnionService rezultatul când e gata.
        if (result != null) sendMessage(result)
    }

    fun sendMessage(msg: String) {
        println("message: ")
        println(msg)
        this.amqpTemplate.convertAndSend(connectionFactory.getExchange(),
            connectionFactory.getRoutingKey(),
            msg)
    }

    private fun generateStack(count: Int): Stack? {
        if (count < 1) return null
        var X: MutableSet<Int> = mutableSetOf()
        while (X.count() < count) X.add(primeGenerator.generatePrimeNumber())
        return Stack(X)
    }

    private fun computeExpression(): String? {
        if (A == null) A = generateStack(20)
        if (B == null) B = generateStack(20)

        if (A!!.data.count() == B!!.data.count()) {
            // Împachetăm A și B
            val payload = CartesianPayload(A!!.data, B!!.data)
            val jsonPayload = mapper.writeValueAsString(payload)

            // Trimitem pe coada primului serviciu
            this.amqpTemplate.convertAndSend("cartesian_queue", jsonPayload)

            // Returnăm null pentru a nu declanșa sendMessage din recieveMessage
            return null
        }
        return "compute~" + "Error: A.count() != B.count()"
    }

    private fun regenerateA(): String {
        A = generateStack(20)
        return "A~" + A?.data.toString()
    }

    private fun regenerateB(): String {
        B = generateStack(20)
        return "B~" + B?.data.toString()
    }
}