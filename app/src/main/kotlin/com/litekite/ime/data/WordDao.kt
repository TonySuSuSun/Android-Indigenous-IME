package com.litekite.ime.data

import androidx.room.Dao
import androidx.room.Query

@Dao
interface WordDao {

    // 根據輸入前綴查詢候選詞（依頻率排序）
    @Query("SELECT * FROM words WHERE prefix LIKE :input || '%' ORDER BY frequency DESC LIMIT 20")
    suspend fun getCandidates(input: String): List<WordEntity>

    // 用戶選字後，更新頻率
    @Query("UPDATE words SET frequency = frequency + 1 WHERE word = :word")
    suspend fun incrementFrequency(word: String)
}