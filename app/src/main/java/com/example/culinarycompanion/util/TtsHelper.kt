package com.example.culinarycompanion.util

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsHelper(context: Context) : TextToSpeech.OnInitListener {

    private val tts: TextToSpeech = TextToSpeech(context, this)
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized = _isInitialized.asStateFlow()

    // --- CHANGE 1: A public property to hold the callback function ---
    // The ViewModel will set this property.
    var onSpeechDone: (() -> Unit)? = null

    init {
        // --- CHANGE 2: Set the listener to track speech progress ---
        // This listener will notify us when the TTS engine is done speaking.
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // This is called when speech starts. We don't need it for now.
            }

            override fun onDone(utteranceId: String?) {
                // This is called when speech is finished.
                // We invoke our callback to signal that it's time to listen for a voice command.
                onSpeechDone?.invoke()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                // This is called if there's an error.
                // You could also invoke the callback here if you want to listen even after an error.
            }
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            } else {
                _isInitialized.value = true
                Log.d("TTS", "TTS Helper initialized successfully.")
            }
        } else {
            Log.e("TTS", "TTS Initialization Failed!")
        }
    }

    fun speak(text: String) {
        if (_isInitialized.value) {
            // --- CHANGE 3: We must provide a unique ID for each "speak" call ---
            // This is required for the onDone listener to be triggered correctly.
            val utteranceId = this.hashCode().toString()
            val bundle = Bundle()
            // The KEY_PARAM_UTTERANCE_ID is what links this call to the listener.
            bundle.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)

            tts.speak(text, TextToSpeech.QUEUE_FLUSH, bundle, utteranceId)
        } else {
            Log.w("TTS", "Speak called before TTS was initialized.")
        }
    }

    fun stop() {
        if (_isInitialized.value) {
            tts.stop()
            tts.shutdown()
        }
    }
}