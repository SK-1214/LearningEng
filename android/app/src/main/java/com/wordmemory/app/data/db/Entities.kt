package com.wordmemory.app.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Data model from wiki/tech/storage.md. A word is stored once (by [key]) even when several lessons
 * contain it, so learning records survive re-uploads and are shared across lessons.
 */
@Entity(tableName = "words", indices = [Index(value = ["key"], unique = true)])
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String,
    val text: String,
    val meaning: String,
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sortOrder: Int,
)

@Entity(
    tableName = "lesson_words",
    primaryKeys = ["lessonId", "position"],
    foreignKeys = [
        ForeignKey(LessonEntity::class, ["id"], ["lessonId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(WordEntity::class, ["id"], ["wordId"]),
    ],
    indices = [Index("wordId")],
)
data class LessonWordEntity(
    val lessonId: Long,
    val position: Int,
    val wordId: Long,
    /** Meaning as written for this lesson; the same word can have a story-specific meaning. */
    val meaning: String,
)

enum class StudyMode { LISTEN, CARD, TYPE }

@Entity(
    tableName = "attempts",
    foreignKeys = [ForeignKey(WordEntity::class, ["id"], ["wordId"])],
    indices = [Index("wordId")],
)
data class AttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val wordId: Long,
    val mode: StudyMode,
    val correct: Boolean,
    val answer: String,
    val timestamp: Long,
)

/** Row for the lesson picker. [masteredCount] = words whose latest typing attempt was correct. */
data class LessonSummary(
    val id: Long,
    val name: String,
    val wordCount: Int,
    val masteredCount: Int,
)

data class StudyWord(
    val wordId: Long,
    val position: Int,
    val text: String,
    val meaning: String,
)
