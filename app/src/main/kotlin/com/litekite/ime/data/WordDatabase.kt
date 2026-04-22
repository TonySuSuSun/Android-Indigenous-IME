package com.litekite.ime.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WordEntity::class], version = 1, exportSchema = false)
abstract class WordDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao

    companion object {
        @Volatile
        private var INSTANCE: WordDatabase? = null
        @Volatile
        private var currentLanguage: String? = null

        fun getInstance(context: Context, language: String): WordDatabase {
            // 如果切換了語言，關閉舊的連線
            if (currentLanguage != language) {
                INSTANCE?.close()
                INSTANCE = null
            }

            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WordDatabase::class.java,
                    language
                )
                    .createFromAsset("$language.db")
                    .build()
                    .also {
                        INSTANCE = it
                        currentLanguage = language
                    }
            }
        }

        fun close() {
            INSTANCE?.close()
            INSTANCE = null
            currentLanguage = null
        }
    }
}