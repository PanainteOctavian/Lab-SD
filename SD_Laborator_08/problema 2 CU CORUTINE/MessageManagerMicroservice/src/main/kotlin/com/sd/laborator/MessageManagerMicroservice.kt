package com.sd.laborator

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.*

class MessageManagerMicroservice {
    private val subscribers: HashMap<Int, Socket>
    private lateinit var messageManagerSocket: ServerSocket
    private val mutex = Mutex()

    companion object Constants {
        const val MESSAGE_MANAGER_PORT = 1500
    }

    init {
        subscribers = hashMapOf()
    }

    private suspend fun broadcastMessage(message: String, except: Int) {
        mutex.withLock {
            subscribers.forEach {
                it.takeIf { it.key != except }
                    ?.value?.getOutputStream()?.write((message + "\n").toByteArray())
            }
        }
    }

    private suspend fun respondTo(destination: Int, message: String) {
        mutex.withLock {
            subscribers[destination]?.getOutputStream()?.write(
                (message + "\n").toByteArray())
        }
    }

    private suspend fun handleClient(clientConnection: Socket) {
        println("Subscriber conectat: " +
                "${clientConnection.inetAddress.hostAddress}:${clientConnection.port}")

        mutex.withLock {
            subscribers[clientConnection.port] = clientConnection
        }

        val bufferReader = BufferedReader(InputStreamReader(clientConnection.inputStream))

        try {
            while (true) {
                val receivedMessage = withContext(Dispatchers.IO) { bufferReader.readLine() }

                if (receivedMessage == null) {
                    println("Subscriber-ul ${clientConnection.port} a fost deconectat.")
                    mutex.withLock { subscribers.remove(clientConnection.port) }
                    break
                }

                println("Primit mesaj: $receivedMessage")

                val parts = receivedMessage.split(" ", limit = 3)
                if (parts.size < 3) continue

                val (messageType, messageDestination, messageBody) = parts

                when (messageType) {
                    "intrebare" -> {
                        broadcastMessage(
                            "intrebare ${clientConnection.port} $messageBody",
                            except = clientConnection.port
                        )
                    }
                    "raspuns" -> {
                        respondTo(messageDestination.toInt(), messageBody)
                        val grade = (5..10).random()
                        broadcastMessage(
                            "nota ${clientConnection.port} $grade",
                            except = messageDestination.toInt()
                        )
                    }
                }
            }
        } finally {
            runCatching { bufferReader.close() }
            runCatching { clientConnection.close() }
        }
    }

    fun run() {
        messageManagerSocket = ServerSocket(MESSAGE_MANAGER_PORT)
        println("MessageManagerMicroservice se executa pe portul:" +
                " ${messageManagerSocket.localPort}")
        println("Se asteapta conexiuni si mesaje...")

        runBlocking {
            while (true) {
                val clientConnection = withContext(Dispatchers.IO) {
                    messageManagerSocket.accept()
                }

                launch(Dispatchers.IO) {
                    handleClient(clientConnection)
                }
            }
        }
    }
}

fun main() {
    val messageManagerMicroservice = MessageManagerMicroservice()
    messageManagerMicroservice.run()
}
