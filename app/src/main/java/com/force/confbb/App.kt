package com.force.confbb

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

//todo перевірити поведінку, коли вручну забрано дозволи, та вимкнено блютуз
//todo стан після надання дозволу - пустий екран
@HiltAndroidApp
class App: Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize any libraries or components here if needed
    }
}
