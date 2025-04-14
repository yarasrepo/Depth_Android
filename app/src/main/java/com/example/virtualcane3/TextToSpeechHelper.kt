package com.example.virtualcane3

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TextToSpeechHelper(context: Context) {
    private var textToSpeech: TextToSpeech? = null

    init{
        textToSpeech = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech?.language = Locale.US
            }
        }
    }

    fun speak(text: String){
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun shutdown(){
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}