package com.sd.laborator.persistence.mappers

import com.sd.laborator.models.CacheEntity
import java.sql.ResultSet
import java.sql.SQLException
import org.springframework.jdbc.core.RowMapper

class CacheRowMapper : RowMapper<CacheEntity?> {
    @Throws(SQLException::class)
    override fun mapRow(rs: ResultSet, rowNum: Int): CacheEntity {
        return CacheEntity(rs.getInt("timestamp"),
            rs.getString("query"),
            rs.getString("result")
            )
    }
}