package com.example.matchmovie.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FilmDAO{
    @Insert
    suspend fun insert(film: UserMovie)

    @Query("SELECT * FROM UserMovie")
    suspend fun getAll(): List<UserMovie>


    // Recupero dei film salvati per uno specifico utente
    @Query("SELECT * FROM UserMovie WHERE userId = :userId")
    suspend fun getMoviesByUser(userId: Int): List<UserMovie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM User WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM User WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUser(): User?

    @Query("UPDATE User SET isLoggedIn = 0")
    suspend fun logoutAllUsers()

    @Query("UPDATE User SET isLoggedIn = 1 WHERE email = :email")
    suspend fun setUserLoggedIn(email: String)
}
