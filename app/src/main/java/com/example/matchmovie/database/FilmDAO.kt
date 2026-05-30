package com.example.matchmovie.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FilmDAO{
    @Insert
    suspend fun insert(film: UserMovie)

    @Query("SELECT * FROM UserMovie")
    suspend fun getAll(): List<UserMovie>
}
