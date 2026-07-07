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

    @Query("DELETE FROM UserMovie WHERE _id = :movieId")
    suspend fun deleteUserMovie(movieId: Int)


    // Query per recuperare un film dato il suo id
    // Dato lo user_id, controllo se lo stesso utente sta cercando di salvare un duplicato
    @Query("SELECT EXISTS(SELECT 1 FROM UserMovie WHERE userId = :userId AND tmdbMovieId = :tmdbMovieId)")
    suspend fun isMovieSaved(userId: Int, tmdbMovieId: Int): Boolean


    // Recupero delle serie TV salvate per uno specifico utente
    @Insert
    suspend fun insert(tvSerie: UserTvSerie)

    @Query("SELECT * FROM UserTvSerie")
    suspend fun getAllTvSeries(): List<UserTvSerie>

    @Query("SELECT * FROM UserTvSerie WHERE userId = :userId")
    suspend fun getTvSeriesByUser(userId: Int): List<UserTvSerie>

    @Query("DELETE FROM UserTvSerie WHERE _id = :serieId")
    suspend fun deleteUserTvSerie(serieId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM UserTvSerie WHERE userId = :userId AND tmdbSerieId = :tmdbSerieId)")
    suspend fun isTvSerieSaved(userId: Int, tmdbSerieId: Int): Boolean

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

    @Query("UPDATE User SET profileImage = :profileImage, bio = :bio WHERE _id = :userId")
    suspend fun updateUserProfile(userId: Int, profileImage: String?, bio: String?)

    @Query("SELECT * FROM ApiCacheEntry WHERE cacheKey = :cacheKey LIMIT 1")
    suspend fun getCacheEntry(cacheKey: String): ApiCacheEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCacheEntry(cacheEntry: ApiCacheEntry)

    @Query("DELETE FROM ApiCacheEntry WHERE cacheKey = :cacheKey")
    suspend fun deleteCacheEntry(cacheKey: String)

    @Query("DELETE FROM ApiCacheEntry WHERE fetchedAtMillis < :oldestAllowedMillis")
    suspend fun deleteCacheEntriesOlderThan(oldestAllowedMillis: Long)
}
