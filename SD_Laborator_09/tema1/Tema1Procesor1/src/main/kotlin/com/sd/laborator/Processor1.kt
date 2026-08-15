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
        val parti = mesaj?.split("|")
            ?: throw RuntimeException("Mesaj null")

        // ls -l /home/...
        val primaParte = parti[0].trim()

        val restComanda = parti.drop(1).joinToString("|")

        val rezultat = Runtime.getRuntime()
            .exec(arrayOf("bash", "-c", primaParte))
            .inputStream
            .bufferedReader()
            .readText()
            .trim()

        return "$rezultat###$restComanda"
    }
}

fun main(args: Array<String>) {
    runApplication<SpringDataFlowTimeProcessorApplication>(*args)
}