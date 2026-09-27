package com.wordmemory.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CardPuzzleTest {
    /** Places cards so the letters spell [letters] in order. */
    private fun CardPuzzle.spell(letters: String): CardPuzzle =
        letters.fold(this) { p, ch -> p.place(p.pool.first { it.letter == ch }.id) }

    @Test
    fun correctWhenLettersMatch() {
        val p = CardPuzzle.create("sun", Random(1))
        assertFalse(p.isComplete)
        val done = p.spell("sun")
        assertTrue(done.isComplete)
        assertTrue(done.isCorrect)
    }

    @Test
    fun wrongOrderIsCompleteButNotCorrect() {
        val done = CardPuzzle.create("sun", Random(1)).spell("nus")
        assertTrue(done.isComplete)
        assertFalse(done.isCorrect)
    }

    @Test
    fun repeatedLettersCanBeSwapped() {
        val p = CardPuzzle.create("ball", Random(2))
        val ls = p.pool.filter { it.letter == 'l' }
        val b = p.pool.first { it.letter == 'b' }
        val a = p.pool.first { it.letter == 'a' }
        // Place the two l cards in reverse id order: still correct.
        val done = p.place(b.id).place(a.id).place(ls[1].id).place(ls[0].id)
        assertTrue(done.isCorrect)
    }

    @Test
    fun spacesAreFixedSlotsNotCards() {
        val p = CardPuzzle.create("take off", Random(3))
        assertEquals(7, p.cards.size)
        assertFalse(4 in p.letterSlots)
        assertTrue(p.spell("takeoff").isCorrect)
    }

    @Test
    fun longWordsStartWithFirstLetterHint() {
        val p = CardPuzzle.create("basketball", Random(4))
        assertEquals(setOf(0), p.hintSlots)
        assertEquals('b', p.letterAt(0))
        assertEquals(9, p.pool.size)
        assertEquals(p, p.remove(0))
        assertEquals('b', p.spell("asketball").reset().letterAt(0))
        assertTrue(p.spell("asketball").isCorrect)
    }

    @Test
    fun shortWordsHaveNoHint() {
        assertTrue(CardPuzzle.create("sweater", Random(5)).hintSlots.isEmpty())
    }

    @Test
    fun removeReturnsCardToPool() {
        val p = CardPuzzle.create("cold", Random(6)).spell("co")
        val back = p.remove(1)
        assertEquals(null, back.letterAt(1))
        assertEquals(3, back.pool.size)
        // The next card goes into the first empty slot again.
        assertEquals('o', back.place(back.pool.first { it.letter == 'o' }.id).letterAt(1))
    }

    @Test
    fun cardsAreNotDealtInAnswerOrder() {
        repeat(20) { seed ->
            val p = CardPuzzle.create("strong", Random(seed))
            assertNotEquals("strong", p.cards.joinToString("") { it.letter.toString() })
        }
    }

    @Test
    fun mixedCaseAndExtraSpacesAreCleaned() {
        val p = CardPuzzle.create("  Movie   Star ", Random(7))
        assertEquals("movie star", p.answer)
    }
}
