package com.wordmemory.app.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/** Wraps the device TTS engine for US English at a child-friendly speed (wiki/tech/pronunciation-tts.md). */
class Speaker(context: Context) {
    enum class Status { INITIALIZING, READY, MISSING_DATA, UNAVAILABLE }

    private val _status = MutableStateFlow(Status.INITIALIZING)
    val status: StateFlow<Status> = _status.asStateFlow()

    private lateinit var tts: TextToSpeech

    init {
        tts = TextToSpeech(context.applicationContext, ::onInit)
    }

    private fun onInit(code: Int) {
        if (code != TextToSpeech.SUCCESS) {
            _status.value = Status.UNAVAILABLE
            return
        }
        _status.value = when (tts.setLanguage(Locale.US)) {
            TextToSpeech.LANG_MISSING_DATA, TextToSpeech.LANG_NOT_SUPPORTED -> Status.MISSING_DATA
            else -> {
                tts.setSpeechRate(SPEECH_RATE)
                Status.READY
            }
        }
    }

    fun speak(text: String) {
        if (_status.value != Status.READY) return
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "word")
    }

    fun stop() {
        if (_status.value == Status.READY) tts.stop()
    }

    private companion object {
        const val SPEECH_RATE = 0.8f
    }
}
