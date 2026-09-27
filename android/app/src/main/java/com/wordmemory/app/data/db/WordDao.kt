package com.wordmemory.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Correct flag (1/0) of word `w`'s most recent typing attempt, or NULL if never typed. */
private const val LATEST_TYPE_RESULT = """
    SELECT a.correct FROM attempts a
    WHERE a.wordId = w.id AND a.mode = 'TYPE'
    ORDER BY a.timestamp DESC, a.id DESC LIMIT 1
"""

@Dao
interface WordDao {
    @Query(
        """
        SELECT l.id, l.name,
            (SELECT COUNT(*) FROM lesson_words lw WHERE lw.lessonId = l.id) AS wordCount,
            (SELECT COUNT(*) FROM lesson_words lw WHERE lw.lessonId = l.id AND (
                SELECT a.correct FROM attempts a
                WHERE a.wordId = lw.wordId AND a.mode = 'TYPE'
                ORDER BY a.timestamp DESC, a.id DESC LIMIT 1
            ) = 1) AS masteredCount
        FROM lessons l
        ORDER BY l.sortOrder
        """
    )
    fun observeLessons(): Flow<List<LessonSummary>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun lesson(lessonId: Long): LessonEntity?

    @Query(
        """
        SELECT w.id AS wordId, lw.position, w.text, lw.meaning
        FROM lesson_words lw JOIN words w ON w.id = lw.wordId
        WHERE lw.lessonId = :lessonId
        ORDER BY lw.position
        """
    )
    suspend fun lessonWords(lessonId: Long): List<StudyWord>

    /**
     * Words in the current lessons whose latest level-3 (typing) attempt was wrong. Getting a word
     * right later removes it, so the list is "words still to fix", not a history of mistakes.
     */
    @Query(
        """
        SELECT w.id AS wordId, MIN(lw.position) AS position, w.text, w.meaning
        FROM words w JOIN lesson_words lw ON lw.wordId = w.id
        WHERE ($LATEST_TYPE_RESULT) = 0
        GROUP BY w.id
        ORDER BY w.text
        """
    )
    suspend fun wrongWords(): List<StudyWord>

    @Query(
        """
        SELECT COUNT(DISTINCT w.id)
        FROM words w JOIN lesson_words lw ON lw.wordId = w.id
        WHERE ($LATEST_TYPE_RESULT) = 0
        """
    )
    fun observeWrongWordCount(): Flow<Int>

    /** Every distinct word in the current lessons, for contest practice across all lessons. */
    @Query(
        """
        SELECT w.id AS wordId, MIN(lw.position) AS position, w.text, w.meaning
        FROM words w JOIN lesson_words lw ON lw.wordId = w.id
        GROUP BY w.id
        ORDER BY w.text
        """
    )
    suspend fun allLessonWords(): List<StudyWord>

    @Query("SELECT COUNT(DISTINCT wordId) FROM lesson_words")
    fun observeDistinctWordCount(): Flow<Int>

    @Query("SELECT * FROM words WHERE `key` = :key")
    suspend fun findWord(key: String): WordEntity?

    @Insert
    suspend fun insertWord(word: WordEntity): Long

    @Update
    suspend fun updateWord(word: WordEntity)

    @Insert
    suspend fun insertLesson(lesson: LessonEntity): Long

    @Insert
    suspend fun insertLessonWords(rows: List<LessonWordEntity>)

    @Query("DELETE FROM lesson_words")
    suspend fun clearLessonWords()

    @Query("DELETE FROM lessons")
    suspend fun clearLessons()

    @Insert
    suspend fun insertAttempt(attempt: AttemptEntity)
}
