package com.litekite.ime.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "word")
    val word: String,

    @ColumnInfo(name = "root", defaultValue = "")
    val root: String = "",

    @ColumnInfo(name = "frequency", defaultValue = "0")
    val frequency: Int = 0,

    @ColumnInfo(name = "source", defaultValue = "system")
    val source: String = "system"
)
