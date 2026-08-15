package com.sd.laborator

import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.runtime.Micronaut
import jakarta.inject.Inject

/**
 * Punct de intrare pentru testarea locala a functiei consumer.
 * Porneste un server HTTP Netty care permite testarea prin cereri POST.
 *
 * Folositi profilul Maven "local":
 *   mvn mn:run -Plocal
 *
 * Exemple de testare:
 *   # Procesare numere direct prin JSON (fara coada):
 *   curl -X POST http://localhost:8082/ \
 *     -H 'Content-Type: application/json' \
 *     -d '{"maxNumber": 200, "numbersToCheck": [7, 13, 20, 97, 100, 4, 11]}'
 *
 *   # Interogare stare acumulator (cate numere asteapta din coada):
 *   curl http://localhost:8082/status
 *
 *   # Procesare numere din coada RabbitMQ (drain + verificare Eratostene):
 *   curl -X POST http://localhost:8082/ \
 *     -H 'Content-Type: application/json' \
 *     -d '{"maxNumber": 1000000}'
 */
object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.run(Application::class.java, *args)
    }

    @Controller
    class LambdaController {

        @Inject
        private lateinit var numbersAccumulator: NumbersAccumulator

        @Inject
        private lateinit var handler: QueueConsumerFunction  // injectat de Micronaut, nu creat manual

        @Post
        fun execute(@Body request: QueueRequest): QueueResponse {
            return handler.apply(request)
        }

        @Get("/status")
        fun status(): Map<String, Any> {
            return mapOf(
                "accumulatorSize" to numbersAccumulator.size(),
                "message" to "Numere in asteptare in acumulator: ${numbersAccumulator.size()}"
            )
        }
    }
}