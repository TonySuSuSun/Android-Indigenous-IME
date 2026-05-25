package com.litekite.ime.util

import java.util.Locale

object CharUtil {

    /**
     * Cycle through alternate characters of the given character. Return the same character if
     * there is no alternate.
     */
    fun Char.cycleCharacter(locale: Locale): Char {
        return if (Character.isUpperCase(this)) {
            this.lowercase(locale)[0]
        } else {
            this.uppercase(locale)[0]
        }
    }
}
