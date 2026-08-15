package com.sd.laborator

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.streaming.Durations
import org.apache.spark.streaming.api.java.JavaStreamingContext
import org.apache.spark.streaming.kafka010.*
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.*
import org.apache.spark.sql.types.DataTypes
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.api.java.JavaInputDStream

fun main() {
    val kafkaParams = mutableMapOf<String, Any>(
        "bootstrap.servers" to "localhost:9092",
        "key.deserializer" to StringDeserializer::class.java,
        "value.deserializer" to StringDeserializer::class.java,
        "group.id" to "analiza_dispersie_group",
        "auto.offset.reset" to "earliest",
        "enable.auto.commit" to true
    )

    val sparkConf = SparkConf().setMaster("local[4]").setAppName("DispersieStreaming")
    val streamingContext = JavaStreamingContext(sparkConf, Durations.seconds(5)) // Loturi de 5 secunde

    // Instanțiem sesiunea Spark SQL pentru a procesa loturile ca DataFrame-uri
    val spark = SparkSession.builder().config(sparkConf).orCreate

    // Ne abonăm la topicul unde vin punctele (ex: "puncte-topic")
    val topics = listOf("puncte-topic")

    val stream: JavaInputDStream<ConsumerRecord<String, String>> =
        KafkaUtils.createDirectStream(
        streamingContext,
        LocationStrategies.PreferConsistent(),
        ConsumerStrategies.Subscribe(topics, kafkaParams)
    )

    // Definim schema pentru datele noastre (presupunem format JSON: {"metoda": "A", "valoare": 12.5})
    val schema = StructType()
        .add("metoda", DataTypes.StringType, false)
        .add("valoare", DataTypes.DoubleType, false)

    // Procesăm fiecare RDD primit în fereastra de timp
    stream.foreachRDD { rdd ->
        if (!rdd.isEmpty) {
            // 1. Extragem doar string-ul (valoarea mesajului din Kafka)
            val jsonRDD = rdd.map { record -> record.value() }

            // 2. Convertim RDD-ul într-un Dataset/DataFrame Spark SQL
            val df = spark.read().schema(schema).json(jsonRDD)

            println("\n--- Lot nou primit la analiza ---")
            df.show(5)

            // 3. METODA STATISTICĂ: Grupăm după metodă și calculăm Varianța/Dispersia (variance) și Deviația Standard (stddev)
            val analizaStatistica = df.groupBy("metoda")
                .agg(
                    count("valoare").`as`("numar_puncte"),
                    mean("valoare").`as`("media"),
                    variance("valoare").`as`("dispersia"),
                    stddev("valoare").`as`("deviatia_standard")
                )

            analizaStatistica.show()

            // 4. Analiză comparativă directă în consolă
            val rezultate = analizaStatistica.collectAsList()
            if (rezultate.size >= 2) {
                val dispA = rezultate.find { it.getAs<String>("metoda") == "A" }?.getAs<Double>("dispersia") ?: 0.0
                val dispB = rezultate.find { it.getAs<String>("metoda") == "B" }?.getAs<Double>("dispersia") ?: 0.0

                println("==== REZULTAT COMPARATIV ====")
                if (dispA > dispB) {
                    println("Metoda A are o dispersie mai mare ($dispA) decât Metoda B ($dispB). Datele sunt mai împrăștiate.")
                } else if (dispB > dispA) {
                    println("Metoda B are o dispersie mai mare ($dispB) decât Metoda A ($dispA). Datele sunt mai împrăștiate.")
                } else {
                    println("Ambele metode au aceeasi dispersie.")
                }
                println("=============================\n")
            }
        }
    }

    // Pornim streaming-ul și îl lăsăm să ruleze continuu
    streamingContext.start()
    streamingContext.awaitTermination()
}