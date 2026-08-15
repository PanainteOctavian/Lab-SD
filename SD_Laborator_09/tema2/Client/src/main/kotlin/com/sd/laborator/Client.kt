package com.sd.laborator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Source
import org.springframework.context.annotation.Bean
import org.springframework.integration.annotation.InboundChannelAdapter
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import org.springframework.integration.annotation.Poller

@EnableBinding(Source::class)
@SpringBootApplication
class ClientMicroservice {
    companion object {
        const val CLIENT_PORT = 1700
    }

    private val comenziPrimite = java.util.concurrent.LinkedBlockingQueue<String>()

    @Bean
    fun socketListener(): ServerSocket {
        val serverSocket = ServerSocket(CLIENT_PORT)
        println("ClientMicroservice asculta pe portul $CLIENT_PORT")

        Thread {
            while (true) {
                val clientConnection = serverSocket.accept()
                Thread {
                    val reader = BufferedReader(
                        InputStreamReader(clientConnection.inputStream))
                    val mesajPrimit = reader.readLine()
                    clientConnection.close()
                    if (mesajPrimit != null) comenziPrimite.put(mesajPrimit)
                }.start()
            }
        }.start()

        return serverSocket
    }

    @Bean
    @InboundChannelAdapter(value = Source.OUTPUT,
        poller = [Poller(fixedDelay = "1000", maxMessagesPerPoll = "1")])
    fun comandaProdus(): () -> Message<String>? {
        return {
            val mesajPrimit = comenziPrimite.poll() // pop()
            if (mesajPrimit != null) {
                val parts = mesajPrimit.split("|")
                val identitateClient = parts[0]
                val adresaLivrare    = parts[1]
                val produsComandat   = parts[2]
                val cantitate        = parts[3]

                // TODO: salvare in baza de date

                // trimite mai departe normal
                val mesaj = "$identitateClient|$produsComandat|$cantitate|$adresaLivrare"
                MessageBuilder.withPayload(mesaj).build()
            } else {
                null
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<ClientMicroservice>(*args)
}