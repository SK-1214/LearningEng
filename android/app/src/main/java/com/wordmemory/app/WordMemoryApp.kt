package com.wordmemory.app

import android.app.Application
import com.wordmemory.app.data.WordRepository
import com.wordmemory.app.data.db.AppDatabase
import com.wordmemory.app.tts.SoundEffects
import com.wordmemory.app.tts.Speaker

/** Manual dependency container; the app is small enough not to need a DI framework. */
class AppContainer(app: Application) {
    val repository = WordRepository(AppDatabase.create(app))
    val speaker = Speaker(app)
    val sounds = SoundEffects()
    val contentResolver = app.contentResolver
}

class WordMemoryApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
