package com.sd.laborator.business.interfaces

import com.sd.laborator.models.Book

interface IJSONPrinter {
    fun printJSON(books: Set<Book>): String
}