package com.sd.laborator.business.services

import com.sd.laborator.business.interfaces.ILibraryDAOService
import com.sd.laborator.models.Book
import com.sd.laborator.models.Content
import com.sd.laborator.persistence.interfaces.ILibraryRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.regex.Pattern

@Service
class LibraryDAOService : ILibraryDAOService {
    @Autowired
    private lateinit var _libraryRepository: ILibraryRepository
    private var _pattern: Pattern = Pattern.compile("\\W")

    override fun createLibraryTable() {
        _libraryRepository.createTable()
    }

    override fun addBook(book: Book) {
        if(_pattern.matcher(book.name).find()) {
            println("SQL Injection for book name")
            return
        }
        _libraryRepository.add(book)
    }

    override fun getBooks(): String {
        val result: Set<Book?> = _libraryRepository.getAll()
        var stringResult: String = ""
        for (item in result) {
            stringResult = stringResult + item + "\n"
        }
        return stringResult
    }

    override fun findAllByAuthor(author: String): String? {
        if(_pattern.matcher(author).find()) {
            println("SQL Injection for author name")
            return null
        }
        val result = _libraryRepository.findByAuthor(author)
        return result.toString()
    }

    override fun findAllByName(name: String): String? {
        if(_pattern.matcher(name).find()) {
            println("SQL Injection for name name")
            return null
        }
        val result = _libraryRepository.findByName(name)
        return result.toString()
    }

    override fun findAllByPublisher(publisher: String): String? {
        if(_pattern.matcher(publisher).find()) {
            println("SQL Injection for publisher name")
            return null
        }
        val result = _libraryRepository.findByPublisher(publisher)
        return result.toString()
    }

    override fun updateBook(book: Book) {
        if(_pattern.matcher(book.name).find()) {
            println("SQL Injection for book name")
            return
        }
        _libraryRepository.update(book)
    }

    override fun deleteBook(name: String) {
        if(_pattern.matcher(name).find()) {
            println("SQL Injection for book name")
            return
        }
        _libraryRepository.delete(name)
    }
}
