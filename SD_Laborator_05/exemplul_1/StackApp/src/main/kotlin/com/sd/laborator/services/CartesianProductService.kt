package com.sd.laborator.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.sd.laborator.interfaces.CartesianProductOperation
import com.sd.laborator.model.CartesianPayload
import com.sd.laborator.model.UnionPayload
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CartesianProductService: CartesianProductOperation {
    @Autowired
    private lateinit var rabbitTemplate: RabbitTemplate

    private val mapper = jacksonObjectMapper()

    @RabbitListener(queues = ["cartesian_queue"])
    fun processCartesian(jsonPayload: String) {

        val payload = mapper.readValue<CartesianPayload>(jsonPayload)

        val partialResult1 = executeOperation(payload.a, payload.b)
        val partialResult2 = executeOperation(payload.b, payload.b)

        val nextPayload = UnionPayload(payload.a, payload.b, partialResult1, partialResult2)
        val nextJsonPayload = mapper.writeValueAsString(nextPayload)

        rabbitTemplate.convertAndSend("union_queue", nextJsonPayload)
    }

    override fun executeOperation(A: Set<Int>, B: Set<Int>): Set<Pair<Int, Int>> {
        var result: MutableSet<Pair<Int, Int>> = mutableSetOf()
        A.forEach { a -> B.forEach { b -> result.add(Pair(a, b)) } }
        return result.toSet()
    }
}