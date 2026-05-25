package com.litekite.ime.util

import com.litekite.ime.app.ImeApp
import java.util.StringTokenizer
import java.util.regex.Pattern

object StringUtil {

    private val TAG = StringUtil::class.java.simpleName

    private val PUNCTUATION_PATTERN = Pattern.compile("[_\\-,.]")

    fun String.parseCSV(): IntArray {
        val size = this.count { char -> char == ',' }
        val keyCodes = IntArray(size)
        val tokenizer = StringTokenizer(this, ",")
        val index = 0
        while (tokenizer.hasMoreTokens()) {
            try {
                keyCodes[index] = tokenizer.nextToken().toInt()
                index.inc()
            } catch (_: NumberFormatException) {
                ImeApp.printLog(TAG, "Error parsing keycodes $this")
            }
        }
        return keyCodes
    }

    fun String.isPunctuation(): Boolean = PUNCTUATION_PATTERN.matcher(this).matches()
}
