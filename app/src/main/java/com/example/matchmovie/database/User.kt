package com.example.matchmovie.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "User",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val _id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "email") var email: String,
    @ColumnInfo(name = "password") var password: String,
    @ColumnInfo(name = "profileImage") var profileImage: String?,
    @ColumnInfo(name = "bio") var bio: String?,
    @ColumnInfo(name = "createdAt") var createdAt: Long,
    @ColumnInfo(name = "isLoggedIn") var isLoggedIn: Boolean,
)
