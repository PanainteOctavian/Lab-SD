package com.sd.laborator
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.Exception
import java.net.Socket
import kotlin.system.exitProcess
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DBMicroservice {
    private var dbFile = File("db.txt")
    private var dbList = HashMap<String, MutableList<Int>>()
    private lateinit var messageManagerSocket: Socket
    private val fileMutex = Mutex()
    companion object Constants {
        val MESSAGE_MANAGER_HOST = System.getenv(
            "MESSAGE_MANAGER_HOST") ?: "localhost"
        const val MESSAGE_MANAGER_PORT = 1500
    }

    private fun subscribeToMessageManager() {
        try {
            messageManagerSocket = Socket(MESSAGE_MANAGER_HOST, MESSAGE_MANAGER_PORT)
            println("M-am conectat la MessageManager!")
        } catch (e: Exception) {
            println("Nu ma pot conecta la MessageManager!")
            exitProcess(1)
        }
    }
    fun run() {
        // microserviciul se inscrie in lista de "subscribers" de la MessageManager
        // prin conectarea la acesta
        subscribeToMessageManager()
        println("DBMicroservice se executa pe portul: ${messageManagerSocket.localPort}")
        println("Se asteapta mesaje...")
        val bufferReader = BufferedReader(InputStreamReader(messageManagerSocket.inputStream))
        runBlocking {
            while (true) {
                // se asteapta intrebari trimise prin intermediarul "MessageManager"
                val response = withContext(Dispatchers.IO) { bufferReader.readLine() }
                    if (response == null) {
                        // daca se primeste un mesaj gol (NULL), atunci inseamna ca
                        // cealalta parte a
                        // socket-ului a fost inchisa
                        println("Microserviciul MessageService  (${messageManagerSocket.port}) " +
                                "a fost oprit.")
                        bufferReader.close()
                        messageManagerSocket.close()

                        fileMutex.withLock {
                            dbFile.appendText("\n--- Medii ---\n")
                            dbList.forEach { (port, grades) ->
                                val average = grades.average()
                                dbFile.appendText("$port: ${"%.2f".format(average)}\n")
                            }
                            dbFile.appendText("-".repeat(20) + "\n")
                        }

                        break
                    }
                    // corutina
                    launch(Dispatchers.IO) {
                        val (messageType, messageDestination, messageBody) =
                            response.split(" ", limit = 3)

                        when (messageType) {
                            "nota" -> {
                                val name = messageDestination
                                val grade = messageBody.trim().toInt()
                                println("$grade pt $name")
                                dbList.getOrPut(name) { mutableListOf() }.add(grade)
                                fileMutex.withLock {
                                    dbFile.appendText("$name $grade\n")
                                }
                            }
                        }
                    }
            }
        }
    }
}

fun main() {
    val DBMicroservice = DBMicroservice()
    DBMicroservice.run()
}