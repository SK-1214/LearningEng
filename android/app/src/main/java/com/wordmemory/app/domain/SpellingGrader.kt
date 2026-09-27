package com.wordmemory.app.domain

/** Grading rules for typing mode (wiki/learning/learning-modes.md, ADR 0004). */
object SpellingGrader {
    /** Case-insensitive; ignores leading/trailing spaces and treats runs of spaces as one. */
    fun isCorrect(answer: String, word: String): Boolean = WordText.key(answer) == WordText.key(word)

    /** Only English letters and spaces can be typed ("take off", "movie star"). */
    fun filterInput(raw: String): String = raw.filter { it in 'a'..'z' || it in 'A'..'Z' || it == ' ' }

    /** True when the keyboard is in Korean mode, so the child can be told to switch to English. */
    fun containsHangul(text: String): Boolean =
        text.any { it in '가'..'힣' || it in 'ㄱ'..'ㆎ' || it in 'ᄀ'..'ᇿ' }

    /** Stars on the result screen: 3 for 90%+, 2 for 60%+, otherwise 1. */
    fun stars(correct: Int, total: Int): Int {
        if (total == 0) return 0
        val rate = correct.toDouble() / total
        return when {
            rate >= 0.9 -> 3
            rate >= 0.6 -> 2
            else -> 1
        }
    }
}
