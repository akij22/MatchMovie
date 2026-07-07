package com.example.matchmovie.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ApiCacheEntry")
data class ApiCacheEntry(
    @PrimaryKey
    @ColumnInfo(name = "cacheKey")
    val cacheKey: String,

    @ColumnInfo(name = "payloadJson")
    val payloadJson: String,

    @ColumnInfo(name = "fetchedAtMillis")
    val fetchedAtMillis: Long
)
