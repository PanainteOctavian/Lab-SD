package com.sd.laborator.business.services

import com.sd.laborator.business.interfaces.ICachingService
import com.sd.laborator.models.CacheEntity
import com.sd.laborator.persistence.interfaces.ICachingRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.regex.Pattern

@Service
class CachingService: ICachingService {
    @Autowired
    private lateinit var _cachingRepository: ICachingRepository
    private val _pattern: Pattern = Pattern.compile("['\";\\\\]|--")

    override fun createCacheTable() {
        _cachingRepository.createTable()
    }

    override fun addToCache(query: String, result:String){
        if(_pattern.matcher(query).find()) {
            println("SQL Injection for query")
            return
        }

        val timestamp = (System.currentTimeMillis() / 1000).toInt()

        // sterge query vechi
        if (_cachingRepository.getByQuery(query) != null) {
            _cachingRepository.delete(query)
        }

        val cacheEntity = CacheEntity(
            timestamp,
            query,
            result
        )

        _cachingRepository.add(cacheEntity)
    }

    override fun exists(query: String): CacheEntity? {
        if(_pattern.matcher(query).find()) {
            println("SQL Injection for query")
            return null
        }
        return _cachingRepository.getByQuery(query)
    }

    override fun isExpired(query: String): Boolean {
        val entity = _cachingRepository.getByQuery(query) ?: return true
        val nowSeconds = (System.currentTimeMillis() / 1000).toInt()
        return (nowSeconds - entity.timestamp) > 3600 // 1 h = 3600 s
    }

    override fun deleteCache(query: String) {
        if(_pattern.matcher(query).find()) {
            println("SQL Injection for query")
            return
        }
        _cachingRepository.delete(query)
    }

}