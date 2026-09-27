package com.wordmemory.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WordEntity::class, LessonEntity::class, LessonWordEntity::class, AttemptEntity::class],
    version = 2,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao

    companion object {
        // v1 only existed during the first tablet test (2026-09-26) and named lessons "levels";
        // it held no real learning history, so it is dropped. Later versions need real migrations.
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "word_memory.db")
                .fallbackToDestructiveMigrationFrom(1)
                .build()
    }
}
