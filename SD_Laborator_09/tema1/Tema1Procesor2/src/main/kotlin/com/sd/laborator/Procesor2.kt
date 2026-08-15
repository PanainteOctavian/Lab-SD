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

        // rezultat de la proc1
        // total 4
        // -rw-rw-r-- 1 octavian octavian 1 mai 10 19:26 da
        // -rw-rw-r-- 1 octavian octavian 0 mai 10 19:22 Nirvana1
        // -rw-rw-r-- 1 octavian octavian 0 mai 10 19:22 Nirvana2
        val rezProc1 = parti[0].trim()

        // grep ''|wc -l
        val restComanda = parti[1].trim()

        val cuvant = restComanda
            .split("|")[0]
            .replace("grep", "")
            .replace("'", "")
            .trim()

        val rezultat = rezProc1
            .lines()
            .filter { it.contains(cuvant) }
            .joinToString("\n")

        val restNou = restComanda
            .split("|")
            .drop(1)
            .joinToString("|") // "wc -l"

        return "$rezultat###$restNou"
    }
}

fun main(args: Array<String>) {
    runApplication<SpringDataFlowTimeProcessorApplication>(*args)
}