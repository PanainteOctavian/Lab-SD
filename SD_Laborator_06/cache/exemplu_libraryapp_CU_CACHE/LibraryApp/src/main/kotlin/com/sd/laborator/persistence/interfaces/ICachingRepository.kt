package com.sd.laborator.persistence.interfaces

import com.sd.laborator.models.CacheEntity

interface ICachingRepository {
    // create
    fun createTable();
    fun add(item: CacheEntity)

    // retrieve
    fun getByQuery(query: String): CacheEntity?

    // update

    // delete dupa TimeStamp
    fun delete(query: String)
}