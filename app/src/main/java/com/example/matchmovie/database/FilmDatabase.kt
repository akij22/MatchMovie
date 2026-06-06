package com.example.matchmovie.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [UserMovie::class],
    version = 2,
    exportSchema = false
)
abstract class FilmDatabase : RoomDatabase() {

    abstract fun getDao(): FilmDAO

    companion object {
        @Volatile
        private var INSTANCE: FilmDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE UserMovie ADD COLUMN release_date TEXT")
            }
        }


        fun getInstance(context: Context): FilmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FilmDatabase::class.java, "film_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }

        }
    }


}
