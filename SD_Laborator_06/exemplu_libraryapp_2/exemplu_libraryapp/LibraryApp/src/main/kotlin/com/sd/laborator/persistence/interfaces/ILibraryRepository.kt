package com.sd.laborator.persistence.interfaces

import com.sd.laborator.models.Book

interface ILibraryRepository {
    // create
    fun createTable()
    fun add(book: Book)

    // read
    fun getAll(): Set<Book?>
    fun findByAuthor(author: String): Set<Book?>
    fun findByName(name: String): Set<Book?>
    fun findByPublisher(publisher: String): Set<Book?>

    // update
    fun update(book: Book)

    // delete
    fun delete(name: String)
}