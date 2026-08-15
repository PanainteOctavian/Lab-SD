package com.sd.laborator
import io.micronaut.core.annotation.Introspected
import io.micronaut.function.aws.MicronautRequestHandler
import java.util.UUID

@Introspected
class BookRequestHandler :
    MicronautRequestHandler<Book1?, BookSaved1?>() {

    override fun execute(input: Book1?): BookSaved1? {
        return if (input != null) {
            val bookSaved = BookSaved1()
            bookSaved.name = input.name
            bookSaved.isbn = UUID.randomUUID().toString()
            return bookSaved
        } else {
            null
        }
    }
}