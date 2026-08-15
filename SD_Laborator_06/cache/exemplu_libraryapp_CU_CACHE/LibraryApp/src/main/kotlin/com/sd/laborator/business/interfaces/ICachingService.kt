package com.sd.laborator.business.interfaces

import com.sd.laborator.models.CacheEntity

interface ICachingService {
    fun createCacheTable()
    fun addToCache(query: String, result:String)
    fun exists(query: String): CacheEntity?
    fun isExpired(query: String): Boolean
    fun deleteCache(query:String)
}