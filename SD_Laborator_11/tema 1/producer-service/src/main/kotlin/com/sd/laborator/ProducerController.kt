package com.sd.laborator

import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException

@Introspected
data class PublishRequest(val filePath: String)

data class PublishResponse(
    val success: Boolean,
    val message: String,
    val publishedCount: Int,
    val publishedNumbers: List<Int>,
    val errors: List<String>
)

@Controller("/publish")
class ProducerController {

    @Inject
    private lateinit var fileReaderService: FileReaderService

    private val LOG: Logger = LoggerFactory.getLogger(ProducerController::class.java)

    @Post
    @Produces(MediaType.APPLICATION_JSON)
    fun publish(@Body request: PublishRequest): HttpResponse<PublishResponse> {
        LOG.info("Cerere primita pentru fisierul: ${request.filePath}")

        return try {
            val result = fileReaderService.readAndPublish(request.filePath)
            val response = PublishResponse(
                success = true,
                message = "Au fost publicate ${result.publishedCount} numere in coada RabbitMQ",
                publishedCount = result.publishedCount,
                publishedNumbers = result.publishedNumbers,
                errors = result.errors
            )
            HttpResponse.ok(response)
        } catch (e: FileNotFoundException) {
            LOG.error("Fisierul nu a fost gasit: ${request.filePath}", e)
            HttpResponse.badRequest(
                PublishResponse(
                    success = false,
                    message = "Fisierul nu a fost gasit: ${request.filePath}",
                    publishedCount = 0,
                    publishedNumbers = emptyList(),
                    errors = listOf(e.message ?: "FileNotFoundException")
                )
            )
        } catch (e: Exception) {
            LOG.error("Eroare la publicarea numerelor", e)
            HttpResponse.serverError(
                PublishResponse(
                    success = false,
                    message = "Eroare interna: ${e.message}",
                    publishedCount = 0,
                    publishedNumbers = emptyList(),
                    errors = listOf(e.message ?: "Unknown error")
                )
            )
        }
    }
}