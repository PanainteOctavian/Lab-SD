import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

class MessageProcessorMicroservice {
    private var messageProcessorSocket: ServerSocket
    private lateinit var biddingProcessorSocket: Socket
    private var auctioneerConnection: Socket
    private var receiveInQueueObservable: Observable<String>
    private val subscriptions = CompositeDisposable()
    private val messageQueue: Queue<Message> = LinkedList<Message>()

    companion object Constants {
        const val MESSAGE_PROCESSOR_PORT = 2100
        const val BIDDING_PROCESSOR_HOST = "localhost"
        const val BIDDING_PROCESSOR_PORT = 2200
        const val JOURNAL_FILE           = "/home/octavian/Documents/Facultate/SD/SD_Laborator_07/" +
                "journal.txt"
        const val MAX_RETRIES            = 10
        const val RETRY_DELAY_MS         = 1_000L
    }

    private val tsFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
    private fun journalWrite(tag: String, text: String) {
        val line = "[${tsFormat.format(Date())}] [$tag] $text"
        println(line)
        File(JOURNAL_FILE).appendText(line + "\n")
    }
    private fun journalLog(text: String) = journalWrite("LOG", text)
    private fun journalMsg(msg: Message) =
        journalWrite("MSG", String(msg.serialize()).trim())
    private fun journalDone() = journalWrite("STATE", "DONE")

    private fun recoverQueue(): List<Message> {
        val file = File(JOURNAL_FILE)
        if (!file.exists()) return emptyList()

        val lines = file.readLines()

        // daca ultima starea = DONE e ok
        val lastState = lines.lastOrNull { it.contains("[STATE]") }
        if (lastState != null && lastState.contains("DONE")) return emptyList()

        // altfel recuperare
        val lastDoneIndex = lines.indexOfLast { it.contains("[STATE]") &&
                it.contains("DONE") }
        val relevant = if (lastDoneIndex == -1) lines else lines.drop(lastDoneIndex + 1)

        return relevant
            .filter { it.contains("[MSG]") }
            .mapNotNull { line ->
                try {
                    val encoded = line.substringAfter("[MSG] ")
                    Message.deserialize(encoded.toByteArray())
                } catch (_: Exception) { null }
            }
    }

    init {
        messageProcessorSocket = ServerSocket(MESSAGE_PROCESSOR_PORT)
        println("MessageProcessorMicroservice se executa pe portul: " +
                "${messageProcessorSocket.localPort}")

        val recovered = recoverQueue()
        if (recovered.isNotEmpty()) {
            journalLog("Recovery: ${recovered.size} mesaje gasite. " +
                    "Reiau trimiterea catre BiddingProcessor.")
            println("[RECOVERY] ${recovered.size} mesaje recuperate." +
                    "Reiau trimiterea...")
            recovered.forEach { messageQueue.add(it) }
            sendProcessedMessages()
        }

        println("Se asteapta mesaje pentru procesare...")
        auctioneerConnection = messageProcessorSocket.accept()
        val bufferReader = BufferedReader(InputStreamReader(auctioneerConnection.inputStream))

        receiveInQueueObservable = Observable.create<String> { emitter ->
            while (true) {
                val receivedMessage = bufferReader.readLine()

                if (receivedMessage == null) {
                    bufferReader.close()
                    auctioneerConnection.close()
                    emitter.onError(Exception("Eroare: AuctioneerMicroservice" +
                            " ${auctioneerConnection.port} a fost deconectat."))
                    break
                }

                if (Message.deserialize(receivedMessage.toByteArray()).body == "final") {
                    emitter.onComplete()
                    break
                } else {
                    emitter.onNext(receivedMessage)
                }
            }
        }
    }

    private fun receiveAndProcessMessages() {
        val receiveInQueueSubscription = receiveInQueueObservable
            .filter {
                val message = Message.deserialize(it.toByteArray())
                val currentValue = message.body.split(" ").last()
                messageQueue.none { it.body.split(" ").last() == currentValue }
            }
            .subscribeBy(
                onNext = {
                    val message = Message.deserialize(it.toByteArray())
                    println(message)
                    messageQueue.add(message)
                },
                onComplete = {
                    val inversedQueue = messageQueue.sortedByDescending { it.timestamp }
                    messageQueue.clear()
                    messageQueue.addAll(inversedQueue)

                    val finishedMessagesMessage = Message.create(
                        "${auctioneerConnection.localAddress}:" +
                                "${auctioneerConnection.localPort}",
                        "am primit tot"
                    )
                    auctioneerConnection.getOutputStream().write(
                        finishedMessagesMessage.serialize())
                    auctioneerConnection.close()

                    journalLog("Coada procesata. Salvez ${messageQueue.size} " +
                            "mesaje inainte de trimitere.")
                    messageQueue.forEach { journalMsg(it) }

                    sendProcessedMessages()
                },
                onError = { println("Eroare: $it") }
            )
        subscriptions.add(receiveInQueueSubscription)
    }

    private fun sendProcessedMessages() {
        var connected = false
        var attempt   = 0

        while (!connected && attempt < MAX_RETRIES) {
            attempt++
            try {
                biddingProcessorSocket = Socket(BIDDING_PROCESSOR_HOST, BIDDING_PROCESSOR_PORT)
                connected = true
            } catch (e: Exception) {
                println("Nu ma pot conecta la BiddingProcessor! " +
                        "(tentativa $attempt/$MAX_RETRIES)")
                Thread.sleep(RETRY_DELAY_MS)
            }
        }

        if (!connected) {
            journalLog("BiddingProcessor indisponibil dupa $MAX_RETRIES tentative.")
            // messageQueue.forEach { journalMsg(it) }
            messageProcessorSocket.close()
            exitProcess(1)
        }

        println("Trimit urmatoarele mesaje:")
        Observable.fromIterable(messageQueue).subscribeBy(
            onNext = {
                println(it.toString())
                biddingProcessorSocket.getOutputStream().write(it.serialize())
            },
            onComplete = {
                val noMoreMessages = Message.create(
                    "${biddingProcessorSocket.localAddress}:" +
                            "${biddingProcessorSocket.localPort}",
                    "final"
                )
                biddingProcessorSocket.getOutputStream().write(noMoreMessages.serialize())
                biddingProcessorSocket.close()

                journalDone()
                journalLog("Toate mesajele trimise catre BiddingProcessor.")
                File(JOURNAL_FILE).writeText("")

                subscriptions.dispose()
            }
        )
    }

    fun run() {
        receiveAndProcessMessages()
    }
}

fun main(args: Array<String>) {
    val messageProcessorMicroservice = MessageProcessorMicroservice()
    messageProcessorMicroservice.run()
}
