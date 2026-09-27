package com.wordmemory.app.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordmemory.app.data.WordRepository
import com.wordmemory.app.data.db.StudyMode
import com.wordmemory.app.data.db.StudyWord
import com.wordmemory.app.domain.SpellingGrader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TypeUiState(
    val loading: Boolean = true,
    val lessonName: String = "",
    /** Changes whenever a new round starts, so the screen re-speaks even when index stays 0. */
    val round: Int = 0,
    val queue: List<StudyWord> = emptyList(),
    val index: Int = 0,
    val input: String = "",
    /** null while typing; true/false once the current word has been graded. */
    val graded: Boolean? = null,
    val koreanKeyboard: Boolean = false,
    val results: List<StudyResult> = emptyList(),
    val finished: Boolean = false,
) {
    val current: StudyWord? get() = queue.getOrNull(index)
    val correctCount: Int get() = results.count { it.correct }
    val wrongWords: List<StudyWord> get() = results.filterNot { it.correct }.map { it.word }
}

/**
 * Typing mode: hear the word, type its spelling with the device keyboard (wiki ADR 0003, 0004).
 * [questionCount] > 0 asks only that many randomly picked words per round (contest practice).
 */
class TypeViewModel(
    lessonId: Long,
    private val repository: WordRepository,
    private val questionCount: Int = 0,
) : ViewModel() {
    private val _state = MutableStateFlow(TypeUiState())
    val state: StateFlow<TypeUiState> = _state.asStateFlow()
    private var allWords: List<StudyWord> = emptyList()

    init {
        viewModelScope.launch {
            val lesson = repository.lessonWords(lessonId)
            allWords = lesson.words
            _state.value = TypeUiState(loading = false, lessonName = lesson.lessonName, queue = pick(allWords))
        }
    }

    fun onInput(raw: String) {
        val s = _state.value
        if (s.graded != null) return
        if (SpellingGrader.containsHangul(raw)) {
            _state.value = s.copy(koreanKeyboard = true)
            return
        }
        _state.value = s.copy(input = SpellingGrader.filterInput(raw), koreanKeyboard = false)
    }

    fun submit() {
        val s = _state.value
        val word = s.current ?: return
        if (s.graded != null || s.input.isBlank()) return
        val correct = SpellingGrader.isCorrect(s.input, word.text)
        _state.value = s.copy(graded = correct, results = s.results + StudyResult(word, correct, s.input.trim()))
        viewModelScope.launch { repository.recordAttempt(word.wordId, StudyMode.TYPE, correct, s.input.trim()) }
    }

    fun next() = _state.update { s ->
        if (s.graded == null) return@update s
        if (s.index + 1 >= s.queue.size) s.copy(finished = true)
        else s.copy(index = s.index + 1, input = "", graded = null, koreanKeyboard = false)
    }

    fun retryWrong() = startRound(_state.value.wrongWords)

    /** A new random pick, so "처음부터 다시" in contest practice asks different words. */
    fun restart() = startRound(pick(allWords))

    private fun pick(words: List<StudyWord>): List<StudyWord> =
        words.shuffled().let { if (questionCount > 0) it.take(questionCount) else it }

    private fun startRound(words: List<StudyWord>) = _state.update { s ->
        s.copy(
            round = s.round + 1,
            queue = words.shuffled(),
            index = 0,
            input = "",
            graded = null,
            koreanKeyboard = false,
            results = emptyList(),
            finished = false,
        )
    }
}
