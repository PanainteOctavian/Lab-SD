package com.sd.laborator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.context.annotation.Bean
import java.net.ServerSocket
import java.util.concurrent.LinkedBlockingQueue

@EnableBinding(Sink::class)
@SpringBootApplication
class LivrareMicroservice {
    companion object {
        const val LIVRARE_PORT = 1701
    }

    // coada pt raspunsuri pt GUI
    private val raspunsPentruGUI = LinkedBlockingQueue<String>()

    // fct care raspunde pt gui
    @Bean
    fun raspunsListener(): ServerSocket {
        val serverSocket = ServerSocket(LIVRARE_PORT)
        println("LivrareMicroservice asculta pe portul $LIVRARE_PORT")

        Thread {
            while (true) {
                val clientConnection = serverSocket.accept()

                Thread {
                    // asteapta pana vine cererea de la GUI si dupa o preia
                    val raspuns = raspunsPentruGUI.take()

                    clientConnection.getOutputStream()
                        .write((raspuns + "\n").toByteArray())
                    clientConnection.close()
                }.start()
            }
        }.start()

        return serverSocket
    }

    @StreamListener(Sink.INPUT)
    fun expediereComanda(comanda: String) {
        println("S-a expediat urmatoarea comanda: $comanda")

        // TODO: salvare in baza de date

        val raspuns = "Comanda reusita: $comanda"
        raspunsPentruGUI.put(raspuns)
    }
}

fun main(args: Array<String>) {
    runApplication<LivrareMicroservice>(*args)
}