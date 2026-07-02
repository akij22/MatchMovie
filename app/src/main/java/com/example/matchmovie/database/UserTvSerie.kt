package com.example.matchmovie.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.matchmovie.enumentity.MovieMood


// Entità salvata su DB nel momento in cui l'utente aggiunge una serie TV nella lista di quelle viste
@Entity(
    tableName = "UserTvSerie",
    indices = [Index(value = ["userId"])]
)
data class UserTvSerie (
    @PrimaryKey(autoGenerate = true) val _id: Int = 0,

    // Associo ogni serie TV ad uno specifico User
    @ColumnInfo(name = "userId") val userId: Int,

    @ColumnInfo(name = "tmdbSerieId") val tmdbSerieId: Int,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "image") var image: String?,
    @ColumnInfo(name = "bio") var bio: String?,
    @ColumnInfo(name = "userRating") var userRating: Int,
    @ColumnInfo(name = "mood") var mood: MovieMood,
    @ColumnInfo(name = "first_air_date") var first_air_date: String?,
)
