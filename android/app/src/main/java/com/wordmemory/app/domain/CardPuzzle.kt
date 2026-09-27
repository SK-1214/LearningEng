package com.wordmemory.app.domain

import kotlin.random.Random

data class LetterCard(val id: Int, val letter: Char)

/**
 * Level 2 (card) puzzle: the word's letters are shuffled into cards and the child taps them into
 * slots in order (wiki/learning/learning-levels.md#레벨-2--카드). Immutable; every action returns a
 * new puzzle.
 *
 * - Spaces ("take off") are fixed slots, not cards.
 * - Long words (8+ letters) start with the first letter placed as a hint.
 * - Grading compares letters, not cards, so the two l's of "ball" can be swapped.
 */
data class CardPuzzle(
    /** Cleaned, lowercase answer. */
    val answer: String,
    /** All letter cards, including the hint card(s). */
    val cards: List<LetterCard>,
    /** slot index (position in [answer]) -> card id placed there. */
    val placed: Map<Int, Int>,
    /** Slot indexes pre-filled as a hint; they cannot be removed. */
    val hintSlots: Set<Int>,
) {
    val letterSlots: List<Int> get() = answer.indices.filter { answer[it] != ' ' }

    val pool: List<LetterCard> get() = cards.filter { it.id !in placed.values }

    val isComplete: Boolean get() = letterSlots.all { it in placed }

    val isCorrect: Boolean get() = isComplete && letterSlots.all { letterAt(it) == answer[it] }

    fun letterAt(slot: Int): Char? = placed[slot]?.let { id -> cards.first { it.id == id }.letter }

    /** Puts [cardId] into the first empty slot. */
    fun place(cardId: Int): CardPuzzle {
        if (cardId in placed.values || cards.none { it.id == cardId }) return this
        val slot = letterSlots.firstOrNull { it !in placed } ?: return this
        return copy(placed = placed + (slot to cardId))
    }

    /** Sends the card in [slot] back to the pool (hint slots stay). */
    fun remove(slot: Int): CardPuzzle =
        if (slot in hintSlots || slot !in placed) this else copy(placed = placed - slot)

    /** Clears everything except the hint, for another try. */
    fun reset(): CardPuzzle = copy(placed = placed.filterKeys { it in hintSlots })

    companion object {
        const val HINT_MIN_LETTERS = 8

        fun create(word: String, random: Random = Random.Default): CardPuzzle {
            val answer = WordText.key(word)
            val letterSlots = answer.indices.filter { answer[it] != ' ' }
            val cards = letterSlots.mapIndexed { id, slot -> LetterCard(id, answer[slot]) }
            val hintSlots = if (letterSlots.size >= HINT_MIN_LETTERS) setOf(letterSlots.first()) else emptySet()
            val placed = hintSlots.associateWith { slot -> letterSlots.indexOf(slot) }
            return CardPuzzle(answer, shuffle(cards, random), placed, hintSlots)
        }

        /** Shuffles so the cards are not already in answer order (when that is possible). */
        private fun shuffle(cards: List<LetterCard>, random: Random): List<LetterCard> {
            if (cards.map { it.letter }.distinct().size < 2) return cards
            val original = cards.map { it.letter }
            repeat(10) {
                val shuffled = cards.shuffled(random)
                if (shuffled.map { it.letter } != original) return shuffled
            }
            return cards.reversed()
        }
    }
}
