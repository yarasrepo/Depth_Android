package com.example.virtualcane3

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

class SpeechToTextHelper(
    private val context: Context,
    private val callback: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null

    fun startListening(fieldName: String) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onResults(results: android.os.Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        matches?.firstOrNull()?.let { callback(it) }
                    }
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.d("SpeechToText", "$fieldName field: Start speaking now.")
                    }
                    override fun onError(error: Int) {
                        Log.e("SpeechToText", "Error: $error")
                    }
                    override fun onEndOfSpeech() {
                        Log.d("SpeechToText", "$fieldName field: Stopped listening.")
                    }
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                    override fun onPartialResults(partialResults: Bundle?) {}
                })
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "$fieldName field: Start speaking now.")
            }
            speechRecognizer?.startListening(intent)

        } else {
            Log.e("SpeechToText", "Speech recognition is not available on this device.")
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }
}
