package com.sd.laborator
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Processor
import org.springframework.integration.annotation.Transformer

@EnableBinding(Processor::class)
@SpringBootApplication
class SpringDataFlowTimeProcessorApplication {
    @Transformer(inputChannel = Processor.INPUT,
        outputChannel=Processor.OUTPUT)
    fun transform(mesaj: String?): Any? {
        // $rezultat###$restComanda
        val parti = mesaj?.split("###")
            ?: throw RuntimeException("Mesaj null")

        // rezultat de la proc2
        // -rw-rw-r-- 1 octavian octavian 0 mai 10 19:22 Nirvana1
        // -rw-rw-r-- 1 octavian octavian 0 mai 10 19:22 Nirvana2
        val rezProc2 = parti[0].trim()

        val rezultat = rezProc2
            .lines()
            .count { it.isNotBlank() }

        return "$rezultat"
    }
}

fun main(args: Array<String>) {
    runApplication<SpringDataFlowTimeProcessorApplication>(*args)
}