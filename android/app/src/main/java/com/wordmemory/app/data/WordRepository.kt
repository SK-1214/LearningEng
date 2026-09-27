package com.wordmemory.app.data

import androidx.room.withTransaction
import com.wordmemory.app.data.db.AppDatabase
import com.wordmemory.app.data.db.AttemptEntity
import com.wordmemory.app.data.db.LessonEntity
import com.wordmemory.app.data.db.LessonSummary
import com.wordmemory.app.data.db.LessonWordEntity
import com.wordmemory.app.data.db.StudyMode
import com.wordmemory.app.data.db.StudyWord
import com.wordmemory.app.data.db.WordEntity
import com.wordmemory.app.domain.WordList
import com.wordmemory.app.domain.WordText
import kotlinx.coroutines.flow.Flow

data class LessonWords(val lessonName: String, val words: List<StudyWord>)

/**
 * A virtual lesson gathering words last answered wrong in level 3, across all lessons. It goes
 * through the same level picker and study screens as a real lesson (wiki/learning/learning-levels.md).
 */
const val WRONG_WORDS_LESSON_ID = -1L
const val WRONG_WORDS_LESSON_NAME = "틀린 단어"

/** A virtual lesson with every word in all lessons, for contest practice (wiki/learning/learning-levels.md). */
const val CONTEST_LESSON_ID = -2L
const val CONTEST_LESSON_NAME = "대회 연습"

class WordRepository(private val db: AppDatabase) {
    private val dao = db.wordDao()

    fun observeLessons(): Flow<List<LessonSummary>> = dao.observeLessons()

    fun observeWrongWordCount(): Flow<Int> = dao.observeWrongWordCount()

    fun observeDistinctWordCount(): Flow<Int> = dao.observeDistinctWordCount()

    suspend fun lessonWords(lessonId: Long): LessonWords {
        if (lessonId == WRONG_WORDS_LESSON_ID) return LessonWords(WRONG_WORDS_LESSON_NAME, dao.wrongWords())
        if (lessonId == CONTEST_LESSON_ID) return LessonWords(CONTEST_LESSON_NAME, dao.allLessonWords())
        val lesson = dao.lesson(lessonId)
        return LessonWords(lesson?.name.orEmpty(), dao.lessonWords(lessonId))
    }

    /**
     * "Replace" upload (MVP supports only this, wiki/product/mvp-roadmap.md): the lesson lists are
     * rebuilt from [list], while words and attempts are kept so learning history is not lost.
     */
    suspend fun replaceAll(list: WordList) = db.withTransaction {
        dao.clearLessonWords()
        dao.clearLessons()
        val lessonIds = list.lessons.mapIndexed { index, name ->
            name to dao.insertLesson(LessonEntity(name = name, sortOrder = index))
        }.toMap()
        val rows = list.entries.map { entry ->
            val wordId = upsertWord(entry.word, entry.meaning)
            LessonWordEntity(lessonIds.getValue(entry.lesson), entry.position, wordId, entry.meaning)
        }
        dao.insertLessonWords(rows)
    }

    private suspend fun upsertWord(text: String, meaning: String): Long {
        val key = WordText.key(text)
        val existing = dao.findWord(key)
        if (existing == null) return dao.insertWord(WordEntity(key = key, text = text, meaning = meaning))
        if (existing.text != text || existing.meaning != meaning) {
            dao.updateWord(existing.copy(text = text, meaning = meaning))
        }
        return existing.id
    }

    suspend fun recordAttempt(wordId: Long, mode: StudyMode, correct: Boolean, answer: String) {
        dao.insertAttempt(
            AttemptEntity(
                wordId = wordId,
                mode = mode,
                correct = correct,
                answer = answer,
                timestamp = System.currentTimeMillis(),
            )
        )
    }
}
