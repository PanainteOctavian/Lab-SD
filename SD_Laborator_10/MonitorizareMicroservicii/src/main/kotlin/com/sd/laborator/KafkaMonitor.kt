package com.sd.laborator

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.PartitionOffset
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.knowm.xchart.XChartPanel
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.style.Styler
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URLEncoder
import java.time.LocalDateTime
import javax.swing.JFrame
import javax.swing.SwingUtilities

@Component
class KafkaMonitor {
    companion object {
        var numberOfBids: Int = 0
        var numberOfProcessedBids: Int = 0
        private var BASE_DOCKER_API_COMMAND = "curl --unix-socket /var/run/docker.sock http:/v1.40"

        private val timeData = mutableListOf<Double>()
        private val bidsData = mutableListOf<Double>()
        private val processedData = mutableListOf<Double>()
        private var secondsElapsed = 0.0

        private val dockerTimeData = mutableMapOf<String,
                MutableList<Double>>()
        private val dockerCpuData = mutableMapOf<String,
                MutableList<Double>>()
        private val dockerMemData = mutableMapOf<String,
                MutableList<Double>>()

        private val kafkaChart = XYChartBuilder()
            .width(800).height(400)
            .title("Kafka - Mesaje in timp real")
            .xAxisTitle("Timp (s)").yAxisTitle("Numar mesaje")
            .theme(Styler.ChartTheme.Matlab)
            .build().also {
                it.addSeries("Oferte primite",
                    listOf(0.0), listOf(0.0))
                it.addSeries("Oferte procesate",
                    listOf(0.0), listOf(0.0))
            }

        private val dockerChart = XYChartBuilder()
            .width(800).height(400)
            .title("Docker - Resurse Auctioneer")
            .xAxisTitle("Timp (s)").yAxisTitle("Memorie (bytes)")
            .theme(Styler.ChartTheme.Matlab)
            .build()

        private var kafkaFrame: JFrame? = null
        private var dockerFrame: JFrame? = null
        private var headless: Boolean = false

        fun initCharts() {
            SwingUtilities.invokeAndWait {
                kafkaFrame = JFrame("Kafka Monitor").apply {
                    defaultCloseOperation = JFrame.EXIT_ON_CLOSE
                    add(XChartPanel(kafkaChart))
                    pack()
                    isVisible = true
                }
                dockerFrame = JFrame("Docker Monitor").apply {
                    defaultCloseOperation = JFrame.EXIT_ON_CLOSE
                    add(XChartPanel(dockerChart))
                    pack()
                    isVisible = true
                }
            }
        }

        fun updateKafkaChart() {
            secondsElapsed += 1.0
            timeData.add(secondsElapsed)
            bidsData.add(numberOfBids.toDouble())
            processedData.add(numberOfProcessedBids.toDouble())

            SwingUtilities.invokeLater {
                kafkaChart.updateXYSeries("Oferte primite", timeData, bidsData, null)
                kafkaChart.updateXYSeries("Oferte procesate", timeData, processedData, null)
                kafkaFrame?.repaint()
            }
        }

        fun updateDockerChart(containerId: String, cpu: Double, mem: Double) {
            if (!dockerTimeData.containsKey(containerId)) {
                dockerTimeData[containerId] = mutableListOf()
                dockerCpuData[containerId] = mutableListOf()
                dockerMemData[containerId] = mutableListOf()
                SwingUtilities.invokeLater {
                    dockerChart.addSeries(containerId, listOf(0.0), listOf(0.0))
                }
            }

            dockerTimeData[containerId]!!.add(secondsElapsed)
            dockerMemData[containerId]!!.add(mem)

            SwingUtilities.invokeLater {
                dockerChart.updateXYSeries(containerId, dockerTimeData[containerId], dockerMemData[containerId], null)
                dockerFrame?.repaint()
            }
        }
    }

    init {
        println("KafkaMonitor instantiated!")
        initCharts()
    }

    @KafkaListener(
        groupId = "KafkaMonitor",
        topicPartitions = [
            TopicPartition(
                topic = "topic_oferte",
                partitionOffsets = [
                    PartitionOffset(partition = "0", initialOffset = "0"),
                    PartitionOffset(partition = "1", initialOffset = "0"),
                    PartitionOffset(partition = "2", initialOffset = "0"),
                    PartitionOffset(partition = "3", initialOffset = "0")
                ]
            ),
            TopicPartition(
                topic = "topic_oferte_procesate",
                partitionOffsets = [
                    PartitionOffset(partition = "0", initialOffset = "0")
                ]
            )
        ]
    )
    fun monitorKafkaMessages(message: ConsumerRecord<String, String>) {
        when(message.topic()) {
            "topic_oferte" -> ++numberOfBids
            "topic_oferte_procesate" -> ++numberOfProcessedBids
        }
    }

    @Scheduled(fixedDelay=1000)
    fun showKafkaStats() {
        println("[${LocalDateTime.now()}] Grad incarcare Auctioneer: $numberOfBids oferte primite")
        println("[${LocalDateTime.now()}] Grad incarcare MessageProcessor: $numberOfProcessedBids oferte procesate")

        // actualizare grafic Kafka
        updateKafkaChart()
    }

    @Scheduled(fixedDelay=2000)
    fun showAuctioneerContainersStats() {
        val auctioneerContainersFilter = URLEncoder.encode("{\"ancestor\": [\"auctioneer_docker-auctioneer\"]}", "utf-8")
        val auctioneerListProcess: Process =
            Runtime.getRuntime().exec("$BASE_DOCKER_API_COMMAND/containers/json?filters=$auctioneerContainersFilter")
        val auctioneerListProcessInput = BufferedReader(InputStreamReader(auctioneerListProcess.inputStream))

        val auctioneerListOutput = auctioneerListProcessInput.readLine()
        auctioneerListProcessInput.close()

        val containerIdRegex = Regex("\"Id\":\"([a-f0-9]*)\"")
        containerIdRegex.findAll(auctioneerListOutput).forEach {
            val auctioneerContainerID = it.groupValues[1].take(12)

            val auctioneerContainerStatsProcess: Process =
                Runtime.getRuntime().exec("$BASE_DOCKER_API_COMMAND/containers/$auctioneerContainerID/stats?stream=0")
            val auctioneerContainerStatsProcessInput = BufferedReader(InputStreamReader(auctioneerContainerStatsProcess.inputStream))

            val auctioneerContainerStatsOutput = auctioneerContainerStatsProcessInput.readLine()

            val cpuUsageRegex = Regex("\"cpu_stats\":\\{\"cpu_usage\":\\{\"total_usage\":([0-9]*),")
            val memoryUsageRegex = Regex("\"memory_stats\":\\{\"usage\":([0-9]*),")

            val cpu = cpuUsageRegex.find(auctioneerContainerStatsOutput)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
            val mem = memoryUsageRegex.find(auctioneerContainerStatsOutput)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

            println("[$auctioneerContainerID] Utilizare procesor: $cpu")
            println("[$auctioneerContainerID] Utilizare memorie: $mem")

            // actualizare grafic Docker
            updateDockerChart(auctioneerContainerID, cpu, mem)
        }
    }
}