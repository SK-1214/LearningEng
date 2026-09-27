package com.wordmemory.app.data.xlsx

import com.wordmemory.app.domain.WordListParser
import org.junit.Assert.assertEquals
import org.junit.Test

/** Uses a copy of raw/wordlists/spelling bee contest.xlsx as of 2026-09-27 (wiki/sources/wordlist-spelling-bee.md). */
class XlsxReaderTest {
    private fun rows() = javaClass.classLoader!!.getResourceAsStream("spelling_bee_contest.xlsx")!!
        .use { XlsxReader.readFirstSheet(it) }

    @Test
    fun readsHeaderAndAllRows() {
        val rows = rows()
        assertEquals(listOf("Lesson", "word", "meaning"), rows.first().cells)
        assertEquals(101, rows.size)
        assertEquals(SheetRow(2, listOf("4-1", "strong", "강한")), rows[1])
        assertEquals(SheetRow(91, listOf("12-10", "everyone", "모든 사람")), rows[90])
        // Lesson 3 was appended below lesson 12 on 2026-09-27.
        assertEquals(SheetRow(101, listOf("3-10", "wear", "입다, 바르다")), rows.last())
    }

    @Test
    fun spelledWordListMatchesWiki() {
        val list = WordListParser.parse(rows())
        assertEquals(100, list.entries.size)
        assertEquals(96, list.uniqueWordCount)
        assertEquals((3..12).map { it.toString() }, list.lessons)
        assertEquals(emptyList<Int>(), list.skippedRows)
        assertEquals(10, list.entries.count { it.lesson == "9" })

        val line = list.entries.single { it.lesson == "9" && it.position == 3 }
        assertEquals("line", line.word)
        assertEquals("줄, 선", line.meaning)
        assertEquals("take off", list.entries.single { it.lesson == "4" && it.position == 2 }.word)
        assertEquals("movie star", list.entries.single { it.lesson == "8" && it.position == 10 }.word)
        assertEquals("~해야 한다", list.entries.single { it.lesson == "10" && it.position == 8 }.meaning)
        assertEquals("~을 해야한다", list.entries.single { it.lesson == "3" && it.position == 9 }.meaning)
    }
}
