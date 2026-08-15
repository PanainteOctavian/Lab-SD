package com.sd.laborator.persistence.repositories

import com.sd.laborator.models.CacheEntity
import com.sd.laborator.persistence.interfaces.ICachingRepository
import com.sd.laborator.persistence.mappers.CacheRowMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.UncategorizedSQLException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

@Repository
class CachingRepository: ICachingRepository {
    @Autowired
    private lateinit var _jdbcTemplate: JdbcTemplate
    private var _rowMapper: RowMapper<CacheEntity?> = CacheRowMapper()

    override fun createTable() {
        _jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS CACHETABLE
            (id INTEGER PRIMARY KEY AUTOINCREMENT,
            timestamp INTEGER,
            query VARCHAR(1000),
            result VARCHAR(1000)
            )
            """
        )
    }

    override fun add(item: CacheEntity) {
        try {
            _jdbcTemplate.update( "INSERT INTO CACHETABLE(timestamp, query, result) " +
                    "VALUES (?, ?, ?)",
                item.timestamp, item.query, item.result)
        } catch (e: UncategorizedSQLException){
            println("An error has occurred in ${this.javaClass.name}.add")
        }
    }

    override fun getByQuery(query: String): CacheEntity?{
        return try {
            //  SELECT * FROM CACHETABLE WHERE query = 'author=Verne'
            _jdbcTemplate.queryForObject("SELECT * FROM CACHETABLE WHERE query = '$query'", _rowMapper)
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }

    override fun delete(query: String) {
        try {
            _jdbcTemplate.update("DELETE FROM CACHETABLE WHERE query = ?", query)
        } catch (e: UncategorizedSQLException){
            println("An error has occurred in ${this.javaClass.name}.delete")
        }
    }

}