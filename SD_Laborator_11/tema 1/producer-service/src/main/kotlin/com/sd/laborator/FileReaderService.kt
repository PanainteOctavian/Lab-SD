package com.sd.laborator

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException

data class PublishResult(
    val filePath: String,
    val publishedNumbers: List<Int>,
    val publishedCount: Int,
    val errors: List<String>
)

@Singleton
class FileReaderService {

    @Inject
    private lateinit var numbersProducer: NumbersProducer

    private val LOG: Logger = LoggerFactory.getLogger(FileReaderService::class.java)

    fun readAndPublish(filePath: String): PublishResult {
        val file = File(filePath)

        if (!file.exists()) {
            LOG.error("Fisierul nu a fost gasit: $filePath")
            throw FileNotFoundException("Fisierul nu a fost gasit: $filePath")
        }

        LOG.info("Se citesc numere din fisierul: $filePath")

        val numbers = mutableListOf<Int>()
        val errors = mutableListOf<String>()

        file.useLines { lines ->
            lines.forEachIndexed { lineIndex, line ->
                val trimmed = line.trim()
                // se ignora liniile goale si comentariile (incepand cu #)
                if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                    val parsed = trimmed.toIntOrNull()
                    if (parsed != null) {
                        numbers.add(parsed)
                        LOG.debug("Se publica numarul $parsed in coada RabbitMQ...")
                        numbersProducer.sendNumber(parsed)
                    } else {
                        val msg = "Linia ${lineIndex + 1}: '$trimmed' nu este un numar intreg valid - ignorat"
                        LOG.warn(msg)
                        errors.add(msg)
                    }
                }
            }
        }

        LOG.info("Au fost publicate ${numbers.size} numere in coada RabbitMQ din fisierul $filePath")

        return PublishResult(
            filePath = filePath,
            publishedNumbers = numbers,
            publishedCount = numbers.size,
            errors = errors
        )
    }
}