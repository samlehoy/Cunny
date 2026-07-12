package com.eleonorez.cunny.ui.compose.components

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

enum class NarrationState {
    IDLE, SPEAKING, PAUSED, ERROR
}

class NarrationManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private val _state = MutableStateFlow(NarrationState.IDLE)
    val state: StateFlow<NarrationState> = _state
    private var pendingText: String? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Set Indonesian locale
            val result = tts?.setLanguage(Locale("id", "ID"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to default locale
                tts?.setLanguage(Locale.getDefault())
            }
            tts?.setSpeechRate(0.95f) // Slightly slower for educational content
            tts?.setPitch(1.05f) // Slightly higher pitch for friendly mascot feel
            isInitialized = true
            
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _state.value = NarrationState.SPEAKING
                }
                override fun onDone(utteranceId: String?) {
                    _state.value = NarrationState.IDLE
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _state.value = NarrationState.ERROR
                }
            })
            
            // If text was queued before init, speak it now
            pendingText?.let { speak(it) }
            pendingText = null
        } else {
            _state.value = NarrationState.ERROR
        }
    }

    fun speak(text: String) {
        if (!isInitialized) {
            pendingText = text
            return
        }
        stop()
        val params = android.os.Bundle()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "cunny_narration")
    }

    fun stop() {
        tts?.stop()
        _state.value = NarrationState.IDLE
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _state.value = NarrationState.IDLE
    }
}
