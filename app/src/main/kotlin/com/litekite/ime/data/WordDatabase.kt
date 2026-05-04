package com.litekite.ime.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [WordEntity::class], version = 1, exportSchema = false)
@TypeConverters()
abstract class WordDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao

    companion object {
        @Volatile
        private var INSTANCE: WordDatabase? = null

        @Volatile
        private var currentLanguage: String? = null

        fun getInstance(context: Context, language: String): WordDatabase {
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
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            android.util.Log.d("IME_DB", "DB onCreate: $language.db")
                        }
                    })
                    .build()
                    .also {
                        INSTANCE = it
                        currentLanguage = language
                        android.util.Log.d("IME_DB", "DB instance created: $language.db")
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