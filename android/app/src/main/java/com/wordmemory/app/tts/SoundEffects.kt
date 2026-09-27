package com.wordmemory.app.tts

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * Short feedback sounds for correct/wrong answers (M5). They are synthesized at startup, so the
 * app ships no audio files: a rising two-note chime for correct, a soft falling tone for wrong.
 */
class SoundEffects {
    private val correct by lazy { staticTrack(tones(880.0 to 0.09, 1318.5 to 0.22)) }
    private val wrong by lazy { staticTrack(tones(330.0 to 0.12, 247.0 to 0.24, volume = 0.25)) }

    fun playCorrect() = play(correct)

    fun playWrong() = play(wrong)

    private fun play(track: AudioTrack?) {
        track ?: return
        runCatching {
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) track.stop()
            track.reloadStaticData()
            track.play()
        }
    }

    private fun staticTrack(samples: ShortArray): AudioTrack? = runCatching {
        AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
            .also { it.write(samples, 0, samples.size) }
    }.getOrNull()

    /** Consecutive sine notes (frequency Hz to seconds), each with a quick attack and exponential decay. */
    private fun tones(vararg notes: Pair<Double, Double>, volume: Double = 0.35): ShortArray {
        val out = ArrayList<Short>()
        for ((freq, seconds) in notes) {
            val n = (SAMPLE_RATE * seconds).toInt()
            val attack = SAMPLE_RATE * 0.005
            for (i in 0 until n) {
                val envelope = min(1.0, i / attack) * exp(-4.0 * i / n)
                val value = sin(2 * PI * freq * i / SAMPLE_RATE) * envelope * volume
                out += (value * Short.MAX_VALUE).toInt().toShort()
            }
        }
        return out.toShortArray()
    }

    private companion object {
        const val SAMPLE_RATE = 44_100
    }
}
