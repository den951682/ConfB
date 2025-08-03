package com.force.test_connection_app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize any libraries or components here if needed
    }
}
