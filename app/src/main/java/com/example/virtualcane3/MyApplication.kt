package com.example.virtualcane3

import com.google.firebase.FirebaseApp
import android.app.Application

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
    }

}