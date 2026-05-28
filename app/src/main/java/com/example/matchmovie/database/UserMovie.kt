package com.example.matchmovie.database


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.matchmovie.enumentity.MovieMood


// Entità salvata su DB nel momento in cui l'utente aggiunge il film nella lista di quelli visti
@Entity(tableName = "UserMovie")
data class UserMovie (
    @PrimaryKey(autoGenerate = true) val _id: Int = 0,
    @ColumnInfo(name = "tmdbMovieId") val tmdbMovieId: Int,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "description") var password: String,
    @ColumnInfo(name = "image") var image: String?,
    @ColumnInfo(name = "bio") var bio: String?,
    @ColumnInfo(name = "rating") var rating: Int,
    @ColumnInfo(name = "mood") var mood: MovieMood,



)