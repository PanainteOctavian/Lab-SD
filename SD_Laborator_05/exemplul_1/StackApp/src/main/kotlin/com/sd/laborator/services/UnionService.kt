package com.sd.laborator.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.sd.laborator.components.RabbitMqConnectionFactoryComponent
import com.sd.laborator.interfaces.UnionOperation
import com.sd.laborator.model.UnionPayload
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UnionService: UnionOperation {
    @Autowired
    private lateinit var rabbitTemplate: RabbitTemplate

    // Avem nevoie de asta ca să știm unde trimitem la final
    @Autowired
    private lateinit var connectionFactory: RabbitMqConnectionFactoryComponent

    private val mapper = jacksonObjectMapper()

    @RabbitListener(queues = ["union_queue"])
    fun processUnionAndFinish(jsonPayload: String) {

        val payload = mapper.readValue<UnionPayload>(jsonPayload)

        val result = executeOperation(payload.p1, payload.p2)

         val finalMessage = "compute~" + "{\"A\": \"" + payload.a.toString() +
                "\", \"B\": \"" + payload.b.toString() +
                "\", \"result\": \"" + result.toString() + "\"}"

        println("Calcul înlănțuit finalizat! Trimit rezultatul...")

       rabbitTemplate.convertAndSend(
            connectionFactory.getExchange(),
            connectionFactory.getRoutingKey(),
            finalMessage
        )
    }

    override fun executeOperation(A: Set<Pair<Int, Int>>, B: Set<Pair<Int, Int>>): Set<Pair<Int, Int>> {
        return A union B
    }
}