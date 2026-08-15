package com.sd.laborator.business.interfaces

import com.sd.laborator.models.Book

interface ILibraryDAOService {
    fun createLibraryTable()

    fun addBook(book: Book)

    fun getBooks(): String

    fun findAllByAuthor(author: String): String?
    fun findAllByName(name: String): String?
    fun findAllByPublisher(publisher: String): String?

    fun updateBook(book: Book)

    fun deleteBook(name: String)
}