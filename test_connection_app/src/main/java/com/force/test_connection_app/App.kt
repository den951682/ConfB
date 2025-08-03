package com.force.test_connection_app

import android.app.Application
import com.force.connection.ConnectionDefaults
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        ConnectionDefaults.log = { tag, message ->
            android.util.Log.d(TAG, message)
        }
    }
}
