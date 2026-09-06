package com.example.data.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _spokenText.value = ""
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _spokenText.value = ""
                }
            })
            tts?.setSpeechRate(0.9f) // Slightly slower rate for clear agricultural guidance
        } else {
            Log.e("VoiceSpeechManager", "TextToSpeech init failed with status: $status")
        }
    }

    fun speak(text: String, langCode: String = "EN") {
        if (!isInitialized || tts == null) {
            return
        }

        stop()

        // Configure speech language
        val locale = when (langCode.uppercase()) {
            "AM", "AMHARIC" -> Locale("am", "ET")
            "OM", "OROMO" -> Locale("om", "ET")
            else -> Locale.US
        }

        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Fallback to English TTS with localized text readout
            tts?.setLanguage(Locale.US)
        }

        _spokenText.value = text
        _isSpeaking.value = true

        val utteranceId = "MEYRA_SPEECH_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        if (tts != null && tts?.isSpeaking == true) {
            tts?.stop()
        }
        _isSpeaking.value = false
        _spokenText.value = ""
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
