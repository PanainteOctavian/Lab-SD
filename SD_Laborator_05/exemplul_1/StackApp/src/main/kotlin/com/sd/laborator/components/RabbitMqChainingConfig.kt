package com.sd.laborator.components

import org.springframework.amqp.core.Queue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMqChainingConfig {

    @Bean
    fun cartesianQueue(): Queue {
        return Queue("cartesian_queue", false)
    }

    @Bean
    fun unionQueue(): Queue {
        return Queue("union_queue", false)
    }
}