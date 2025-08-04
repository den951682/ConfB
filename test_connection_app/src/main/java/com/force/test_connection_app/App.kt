package com.force.test_connection_app

import android.app.Application
import android.util.Log
import com.force.connection.ConnectionDefaults
import com.force.crypto.CryptoDefaults
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        ConnectionDefaults.log = { tag, message -> Log.d(TAG, message) }
        CryptoDefaults.log = { tag, message -> Log.d(TAG, message) }
    }
}
