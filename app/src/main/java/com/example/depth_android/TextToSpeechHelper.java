package com.example.depth_android;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import java.util.Locale;

public class TextToSpeechHelper {

    private TextToSpeech textToSpeech;

    public TextToSpeechHelper(Context context) {
        // Initialize TextToSpeech in the constructor
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int langResult = textToSpeech.setLanguage(Locale.US);
                    if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TextToSpeech", "Language is not supported or missing data.");
                    } else {
                        Log.d("TextToSpeech", "TTS Initialized successfully.");
                    }
                } else {
                    Log.e("TextToSpeech", "Initialization failed.");
                }
            }
        });
    }

    // Speak the text
    public void speakText(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            Log.d("TextToSpeech", "Speaking: " + text);
        } else {
            Log.e("TextToSpeech", "TextToSpeech is not initialized.");
        }
    }

    // Cleanup TextToSpeech when done
    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            Log.d("TextToSpeech", "TTS Shutdown.");
        }
    }
}
