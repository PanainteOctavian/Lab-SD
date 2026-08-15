package com.sd.laborator.persistence.repositories

import com.sd.laborator.models.*
import com.sd.laborator.persistence.interfaces.ILibraryRepository
import com.sd.laborator.persistence.mappers.LibraryRowMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.UncategorizedSQLException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import javax.annotation.PostConstruct

@Repository
class LibraryRepository: ILibraryRepository {
    @Autowired
    private lateinit var _jdbcTemplate: JdbcTemplate
    private var _rowMapper: RowMapper<Book?> = LibraryRowMapper()

    @PostConstruct
    fun init() {
        createTable()
    }

    override fun createTable() {
        _jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS books
            (id INTEGER PRIMARY KEY AUTOINCREMENT,
            author VARCHAR(100),
            name VARCHAR(100),
            publisher VARCHAR(100),
            text VARCHAR(100)
            )
            """
        )
    }

    override fun add(book: Book) {
        try {
            _jdbcTemplate.update( "INSERT INTO books(author, name, publisher, text) " +
                    "VALUES (?, ?, ?, ?)",
                book.author, book.name, book.publisher, book.text)
        } catch (e: UncategorizedSQLException){
            println("An error has occurred in ${this.javaClass.name}.add")
        }
    }

    override fun getAll(): Set<Book?> {
        return _jdbcTemplate.query("SELECT * FROM books", _rowMapper).toSet()
    }

    override fun findByName(name: String): Set<Book?> {
        return _jdbcTemplate.query("SELECT * FROM books WHERE name = '$name'", _rowMapper).toSet()
    }

    override fun findByAuthor(author: String): Set<Book?>{
        return _jdbcTemplate.query("SELECT * FROM books WHERE author = '$author'", _rowMapper).toSet()
    }

    override fun findByPublisher(publisher: String): Set<Book?>{
        return _jdbcTemplate.query("SELECT * FROM books WHERE publisher = '$publisher'", _rowMapper).toSet()
    }

    override fun update(book: Book) {
        try {
            _jdbcTemplate.update("UPDATE books SET author = ?, name = ?, publisher = ?, text = ?" +
                    " WHERE id = ?",
                book.author, book.name, book.publisher, book.text, book.id)
        } catch (e: UncategorizedSQLException){
            println("An error has occurred in ${this.javaClass.name}.update")
        }
    }

    override fun delete(name: String) {
        try {
            _jdbcTemplate.update("DELETE FROM books WHERE name = ?", name)
        } catch (e: UncategorizedSQLException){
            println("An error has occurred in ${this.javaClass.name}.delete")
        }
    }
}