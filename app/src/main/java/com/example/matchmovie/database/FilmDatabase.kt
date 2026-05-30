package com.example.matchmovie.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserMovie::class],
    version = 1,
    exportSchema = false
)
abstract class FilmDatabase : RoomDatabase() {

    abstract fun getDao(): FilmDAO

    companion object {
        @Volatile
        private var INSTANCE: FilmDatabase? = null


        fun getInstance(context: Context): FilmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FilmDatabase::class.java, "film_db"
                )
                    .build()
                INSTANCE = instance
                instance
            }

        }
    }


}
