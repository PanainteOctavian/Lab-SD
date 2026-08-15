from pyspark.sql import SparkSession
import os

if __name__ == '__main__':
    os.environ['PYSPARK_SUBMIT_ARGS'] = '--packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.2.0 pyspark-shell'

    appName = "Kafka Examples"
    master = "local"

    spark = SparkSession.builder \
        .master(master) \
        .appName(appName) \
        .getOrCreate()

    kafka_servers = "localhost:9092"
    topic = "blog"

    df = spark \
        .readStream \
        .format("kafka") \
        .option("kafka.bootstrap.servers", kafka_servers) \
        .option("subscribe", topic) \
        .load()

    df \
        .selectExpr("topic", "CAST(key AS STRING)", "CAST(value AS STRING)", "timestamp") \
        .writeStream \
        .outputMode("append") \
        .format("console") \
        .start() \
        .awaitTermination()
