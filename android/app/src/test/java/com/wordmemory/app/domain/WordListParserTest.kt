package com.wordmemory.app.domain

import com.wordmemory.app.data.xlsx.SheetRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordListParserTest {
    private fun rows(vararg cells: List<String>) = cells.mapIndexed { i, c -> SheetRow(i + 1, c) }

    @Test
    fun findsColumnsByKoreanHeadersInAnyOrder() {
        val list = WordListParser.parse(
            rows(listOf("뜻", "단어", "레벨"), listOf("사과", " apple ", "2-1"), listOf("바나나", "banana", "1-1"))
        )
        assertEquals(listOf("1", "2"), list.lessons)
        assertEquals(WordEntry("2", 1, "apple", "사과"), list.entries[0])
    }

    @Test
    fun withoutLessonColumnEverythingIsOneLesson() {
        val list = WordListParser.parse(rows(listOf("word", "meaning"), listOf("cat", "고양이"), listOf("dog", "개")))
        assertEquals(listOf(WordListParser.DEFAULT_LESSON), list.lessons)
        assertEquals(listOf(1, 2), list.entries.map { it.position })
    }

    @Test
    fun skipsIncompleteRowsAndIgnoresBlankOnes() {
        val list = WordListParser.parse(
            rows(listOf("word", "meaning"), listOf("cat", ""), listOf("", "", ""), listOf("dog", "개"))
        )
        assertEquals(listOf(2), list.skippedRows)
        assertEquals(1, list.entries.size)
    }

    @Test
    fun duplicatePositionsGetTheNextFreeSlot() {
        val list = WordListParser.parse(
            rows(listOf("Lesson", "word", "meaning"), listOf("4-1", "a", "가"), listOf("4-1", "b", "나"))
        )
        assertEquals(listOf(1, 2), list.entries.map { it.position })
    }

    @Test
    fun nonNumberedLessonsKeepTheirName() {
        assertEquals("Animals" to null, WordListParser.splitLesson("Animals"))
        assertEquals("12" to 10, WordListParser.splitLesson("12-10"))
    }

    @Test(expected = WordListException::class)
    fun missingHeaderIsAnError() {
        WordListParser.parse(rows(listOf("apple", "사과")))
    }

    @Test
    fun graderIgnoresCaseAndExtraSpaces() {
        assertTrue(SpellingGrader.isCorrect("  Take   off ", "take off"))
        assertFalse(SpellingGrader.isCorrect("takeoff", "take off"))
        assertEquals("abc d", SpellingGrader.filterInput("a1b-c d!"))
        assertTrue(SpellingGrader.containsHangul("ㅎ"))
        assertEquals(3, SpellingGrader.stars(9, 10))
        assertEquals(2, SpellingGrader.stars(6, 10))
        assertEquals(1, SpellingGrader.stars(5, 10))
    }
}
