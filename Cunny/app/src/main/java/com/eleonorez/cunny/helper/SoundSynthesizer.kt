package com.eleonorez.cunny.helper

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

object SoundSynthesizer {
    private const val SAMPLE_RATE = 44100
    private val playScope = CoroutineScope(Dispatchers.Default)

    enum class SoundType {
        TAP, CORRECT, INCORRECT, SUCCESS, WHOOSH
    }

    fun play(context: Context, type: SoundType) {
        playScope.launch {
            try {
                // Check if sound settings are enabled
                val settingsManager = SettingsManager(context.applicationContext)
                val soundOn = settingsManager.soundFlow.first()
                if (!soundOn) return@launch

                val data = generateSound(type)
                playPcm(data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun generateSound(type: SoundType): ShortArray {
        return when (type) {
            SoundType.TAP -> generateTap()
            SoundType.CORRECT -> generateCorrect()
            SoundType.INCORRECT -> generateIncorrect()
            SoundType.SUCCESS -> generateSuccess()
            SoundType.WHOOSH -> generateWhoosh()
        }
    }

    private fun playPcm(data: ShortArray) {
        val bufferSize = data.size * 2
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
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
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(data, 0, data.size)
        audioTrack.play()

        val durationMs = (data.size.toFloat() / SAMPLE_RATE * 1000).toLong()
        playScope.launch {
            kotlinx.coroutines.delay(durationMs + 100)
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                // Ignore if already released
            }
        }
    }

    private fun generateTap(): ShortArray {
        val durationMs = 60
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val data = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / numSamples
            val freq = 350.0 - (270.0 * progress)
            val envelope = Math.exp(-progress * 5.0) * 0.8
            val sample = sin(2 * PI * freq * t) * envelope
            data[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }

    private fun generateCorrect(): ShortArray {
        val note1Dur = (SAMPLE_RATE * 80 / 1000)
        val note2Dur = (SAMPLE_RATE * 150 / 1000)
        val numSamples = note1Dur + note2Dur
        val data = ShortArray(numSamples)

        for (i in 0 until note1Dur) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / note1Dur
            val freq = 523.25
            val envelope = (1.0 - progress) * 0.7
            val sample = (2.0 * abs((t * freq) % 1.0 - 0.5) - 1.0) * envelope
            data[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }

        for (i in 0 until note2Dur) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / note2Dur
            val freq = 783.99
            val envelope = (1.0 - progress) * 0.7
            val sample = (2.0 * abs((t * freq) % 1.0 - 0.5) - 1.0) * envelope
            data[note1Dur + i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }

        return data
    }

    private fun generateIncorrect(): ShortArray {
        val durationMs = 250
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val data = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / numSamples
            val vibrato = sin(2 * PI * 8.0 * t) * 5.0
            val freq = 120.0 + vibrato
            val envelope = if (progress > 0.8) (1.0 - progress) / 0.2 * 0.5 else 0.5
            val sineVal = sin(2 * PI * freq * t)
            val sample = (if (sineVal >= 0) 1.0 else -1.0) * envelope
            data[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        return data
    }

    private fun generateSuccess(): ShortArray {
        val dur1 = (SAMPLE_RATE * 100 / 1000)
        val dur2 = (SAMPLE_RATE * 100 / 1000)
        val dur3 = (SAMPLE_RATE * 120 / 1000)
        val dur4 = (SAMPLE_RATE * 300 / 1000)
        val numSamples = dur1 + dur2 + dur3 + dur4
        val data = ShortArray(numSamples)

        val freqs = doubleArrayOf(659.25, 783.99, 1046.50, 1318.51)
        val durs = intArrayOf(dur1, dur2, dur3, dur4)

        var offset = 0
        for (note in 0..3) {
            val freq = freqs[note]
            val dur = durs[note]
            for (i in 0 until dur) {
                val t = i.toDouble() / SAMPLE_RATE
                val progress = i.toDouble() / dur
                val envelope = (1.0 - progress) * 0.7
                val sample = (2.0 * abs((t * freq) % 1.0 - 0.5) - 1.0) * envelope
                data[offset + i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
            offset += dur
        }
        return data
    }

    private fun generateWhoosh(): ShortArray {
        val durationMs = 250
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val data = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / numSamples
            val freq = 150.0 + (700.0 * progress)
            val envelope = sin(progress * PI) * 0.7
            val sample = sin(2 * PI * freq * t) * envelope
            data[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        return data
    }
}
