package com.litekite.ime.data

class WordRepository(private val dao: WordDao) {

    suspend fun getCandidates(input: String): List<String> {
        return dao.getCandidates(input).map { it.word }
    }

    suspend fun onWordSelected(word: String) {
        dao.incrementFrequency(word)
    }
}