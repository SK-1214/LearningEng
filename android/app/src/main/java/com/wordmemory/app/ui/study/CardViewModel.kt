package com.wordmemory.app.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordmemory.app.data.WordRepository
import com.wordmemory.app.data.db.StudyMode
import com.wordmemory.app.data.db.StudyWord
import com.wordmemory.app.domain.CardPuzzle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CardPhase {
    /** Placing cards. */
    PLAYING,
    CORRECT,
    /** First wrong try: shown briefly, then the cards go back for another try. */
    RETRY,
    /** Second wrong try: the answer is shown and the child moves on. */
    REVEALED,
}

data class CardUiState(
    val loading: Boolean = true,
    val lessonName: String = "",
    /** Changes whenever a new round starts, so the screen re-speaks even when index stays 0. */
    val round: Int = 0,
    val queue: List<StudyWord> = emptyList(),
    val index: Int = 0,
    val puzzle: CardPuzzle? = null,
    val phase: CardPhase = CardPhase.PLAYING,
    val wrongTries: Int = 0,
    /** What the child made on the first (wrong) try; shown in the wrong-word list instead of the retry. */
    val firstWrongAnswer: String? = null,
    val results: List<StudyResult> = emptyList(),
    val finished: Boolean = false,
) {
    val current: StudyWord? get() = queue.getOrNull(index)
}

/**
 * Level 2 (card): arrange shuffled letter cards (wiki/learning/learning-levels.md#레벨-2--카드).
 * A word counts as correct only when solved on the first try; one retry is allowed before the
 * answer is revealed.
 */
class CardViewModel(lessonId: Long, private val repository: WordRepository) : ViewModel() {
    private val _state = MutableStateFlow(CardUiState())
    val state: StateFlow<CardUiState> = _state.asStateFlow()
    private var allWords: List<StudyWord> = emptyList()

    init {
        viewModelScope.launch {
            val lesson = repository.lessonWords(lessonId)
            allWords = lesson.words
            _state.value = CardUiState(loading = false, lessonName = lesson.lessonName)
            startRound(allWords)
        }
    }

    fun onCard(cardId: Int) = updatePuzzle { it.place(cardId) }

    fun onSlot(slot: Int) = updatePuzzle { it.remove(slot) }

    private fun updatePuzzle(change: (CardPuzzle) -> CardPuzzle) {
        val s = _state.value
        val puzzle = s.puzzle ?: return
        if (s.phase != CardPhase.PLAYING) return
        val next = change(puzzle)
        if (!next.isComplete) {
            _state.value = s.copy(puzzle = next)
            return
        }
        val word = s.current ?: return
        val made = next.answerAsPlaced()
        val firstTry = s.wrongTries == 0
        if (firstTry) {
            viewModelScope.launch { repository.recordAttempt(word.wordId, StudyMode.CARD, next.isCorrect, made) }
        }
        _state.value = when {
            next.isCorrect -> s.copy(
                puzzle = next,
                phase = CardPhase.CORRECT,
                results = s.results + StudyResult(word, firstTry, s.firstWrongAnswer ?: made),
            )
            firstTry -> s.copy(puzzle = next, phase = CardPhase.RETRY, wrongTries = 1, firstWrongAnswer = made)
            else -> s.copy(
                puzzle = next,
                phase = CardPhase.REVEALED,
                wrongTries = s.wrongTries + 1,
                results = s.results + StudyResult(word, false, s.firstWrongAnswer ?: made),
            )
        }
    }

    /** After the "try again" message: cards go back to the pool. */
    fun retry() = _state.update { s ->
        if (s.phase != CardPhase.RETRY) s else s.copy(puzzle = s.puzzle?.reset(), phase = CardPhase.PLAYING)
    }

    fun next() = _state.update { s ->
        if (s.phase != CardPhase.CORRECT && s.phase != CardPhase.REVEALED) return@update s
        if (s.index + 1 >= s.queue.size) s.copy(finished = true)
        else s.copy(
            index = s.index + 1,
            puzzle = CardPuzzle.create(s.queue[s.index + 1].text),
            phase = CardPhase.PLAYING,
            wrongTries = 0,
            firstWrongAnswer = null,
        )
    }

    fun retryWrong() = startRound(_state.value.results.filterNot { it.correct }.map { it.word })

    fun restart() = startRound(allWords)

    private fun startRound(words: List<StudyWord>) = _state.update { s ->
        val queue = words.shuffled()
        s.copy(
            round = s.round + 1,
            queue = queue,
            index = 0,
            puzzle = queue.firstOrNull()?.let { CardPuzzle.create(it.text) },
            phase = CardPhase.PLAYING,
            wrongTries = 0,
            firstWrongAnswer = null,
            results = emptyList(),
            finished = false,
        )
    }
}

/** What the child spelled, e.g. "slow" or "take off", for the attempt record and wrong-word list. */
private fun CardPuzzle.answerAsPlaced(): String =
    answer.indices.joinToString("") { i -> if (answer[i] == ' ') " " else letterAt(i)?.toString() ?: "" }
