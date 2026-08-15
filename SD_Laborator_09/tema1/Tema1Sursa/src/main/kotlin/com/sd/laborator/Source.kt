package com.sd.laborator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Source
import org.springframework.context.annotation.Bean
import org.springframework.integration.annotation.InboundChannelAdapter
import org.springframework.integration.annotation.Poller
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder

@EnableBinding(Source::class)
@SpringBootApplication
class SpringDataFlowTimeSourceApplication {
    @Bean
    @InboundChannelAdapter(value = Source.OUTPUT, poller =
        [Poller(fixedDelay = "10000", maxMessagesPerPoll = "1")])
    fun timeMessageSource(): () -> Message<String> {
        return { MessageBuilder.withPayload("ls -l /home/octavian/Documents/Facultate/SD/SD_Laborator_09/tema1/Nirvana | grep 'Nirvana' | wc -l").build()}}
}
fun main(args: Array<String>) {
    runApplication<SpringDataFlowTimeSourceApplication>(*args)
}
