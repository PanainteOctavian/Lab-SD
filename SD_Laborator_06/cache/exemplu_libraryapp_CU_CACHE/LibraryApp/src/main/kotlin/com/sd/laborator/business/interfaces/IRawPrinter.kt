package com.sd.laborator.business.interfaces

import com.sd.laborator.models.Book

interface IRawPrinter {
    fun printRaw(books: Set<Book>): String
}