package com.sd.laborator.persistence.mappers

import com.sd.laborator.models.*
import java.sql.ResultSet
import java.sql.SQLException
import org.springframework.jdbc.core.RowMapper

class LibraryRowMapper : RowMapper<Book?> {
    @Throws(SQLException::class)
    override fun mapRow(rs: ResultSet, rowNum: Int): Book {
        val content = Content(
            rs.getString("author"),
            rs.getString("text"),
            rs.getString("name"),
            rs.getString("publisher")
        )
        return Book(rs.getInt("id"), content)
    }
}
