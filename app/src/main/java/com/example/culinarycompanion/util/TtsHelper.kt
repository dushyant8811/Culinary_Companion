package com.example.culinarycompanion.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsHelper(context: Context) : TextToSpeech.OnInitListener {

    private val tts: TextToSpeech = TextToSpeech(context, this)
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized = _isInitialized.asStateFlow()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            } else {
                _isInitialized.value = true
            }
        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }

    fun speak(text: String) {
        if (_isInitialized.value) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    fun stop() {
        if (_isInitialized.value) {
            tts.stop()
            tts.shutdown()
        }
    }
}