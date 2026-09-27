package com.wordmemory.app.domain

import com.wordmemory.app.data.xlsx.SheetRow

class WordListException(message: String) : Exception(message)

/** One word as it appears in the uploaded list. [lesson] is the Excel "Lesson" value (wiki ADR 0005). */
data class WordEntry(val lesson: String, val position: Int, val word: String, val meaning: String)

data class WordList(val entries: List<WordEntry>, val skippedRows: List<Int>) {
    /** Lessons in display order: numeric lessons ascending, then others in order of appearance. */
    val lessons: List<String>
        get() = entries.map { it.lesson }.distinct()
            .withIndex()
            .sortedWith(compareBy({ it.value.toIntOrNull() ?: Int.MAX_VALUE }, { it.index }))
            .map { it.value }

    val uniqueWordCount: Int get() = entries.map { WordText.key(it.word) }.distinct().size
}

/**
 * Turns spreadsheet rows into a [WordList]. Columns are found by header name, not position
 * (wiki/product/word-data.md#파싱-규칙-제안): `Lesson`/`Level`, `word`, `meaning`, or Korean equivalents.
 */
object WordListParser {
    private val WORD_HEADERS = setOf("word", "words", "단어", "spelling", "english", "영어")
    private val MEANING_HEADERS = setOf("meaning", "meanings", "뜻", "의미", "korean", "한국어")
    private val LESSON_HEADERS = setOf("lesson", "level", "레슨", "레벨", "unit", "단원")
    private val LESSON_AND_POSITION = Regex("""^(.+?)\s*-\s*(\d+)$""")

    const val DEFAULT_LESSON = "1"

    fun parse(rows: List<SheetRow>): WordList {
        val headerIndex = rows.indexOfFirst { row ->
            row.cells.any { header(it) in WORD_HEADERS } && row.cells.any { header(it) in MEANING_HEADERS }
        }
        if (headerIndex < 0) {
            throw WordListException("첫 줄에 word(단어)와 meaning(뜻) 제목이 있어야 해요")
        }
        val headers = rows[headerIndex].cells.map(::header)
        val wordCol = headers.indexOfFirst { it in WORD_HEADERS }
        val meaningCol = headers.indexOfFirst { it in MEANING_HEADERS }
        val lessonCol = headers.indexOfFirst { it in LESSON_HEADERS }

        val entries = mutableListOf<WordEntry>()
        val skipped = mutableListOf<Int>()
        val usedPositions = mutableMapOf<String, MutableSet<Int>>()

        for (row in rows.drop(headerIndex + 1)) {
            val word = WordText.clean(row.cell(wordCol))
            val meaning = row.cell(meaningCol).trim()
            val lessonCell = if (lessonCol >= 0) row.cell(lessonCol).trim() else ""
            if (word.isEmpty() && meaning.isEmpty() && lessonCell.isEmpty()) continue
            if (word.isEmpty() || meaning.isEmpty()) {
                skipped += row.number
                continue
            }
            val (lesson, requested) = splitLesson(lessonCell)
            val used = usedPositions.getOrPut(lesson) { mutableSetOf() }
            val position = requested?.takeIf { it !in used } ?: ((used.maxOrNull() ?: 0) + 1)
            used += position
            entries += WordEntry(lesson, position, word, meaning)
        }
        if (entries.isEmpty()) throw WordListException("단어를 하나도 찾지 못했어요")
        return WordList(entries, skipped)
    }

    /** "4-1" -> ("4", 1); "Animals" -> ("Animals", null); "" -> (DEFAULT_LESSON, null). */
    fun splitLesson(value: String): Pair<String, Int?> {
        if (value.isEmpty()) return DEFAULT_LESSON to null
        val match = LESSON_AND_POSITION.matchEntire(value) ?: return value to null
        return match.groupValues[1].trim() to match.groupValues[2].toInt()
    }

    private fun header(cell: String) = cell.trim().lowercase()
}

object WordText {
    private val SPACES = Regex("\\s+")

    /** Trims and collapses inner whitespace, keeping case ("take  off " -> "take off"). */
    fun clean(text: String): String = text.trim().replace(SPACES, " ")

    /** Identity of a word across lessons and uploads; learning records are grouped by this. */
    fun key(text: String): String = clean(text).lowercase()
}
