package com.force.confbb.analytics

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics

object AnalyticsLogger {
    private val analytics: FirebaseAnalytics by lazy { Firebase.analytics }

    fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logDeviceSelected(address: String, name: String, terminal: Boolean) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, address)
            putString(FirebaseAnalytics.Param.ITEM_NAME, name)
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, "bluetooth_device")
            putBoolean("in_terminal", terminal)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle)
    }

    fun logDeviceConnected(name: String?, address: String) {
        val bundle = Bundle().apply {
            putString("device_name", name)
            putString("device_address", address)
        }
        analytics.logEvent("device_connected", bundle)
    }


    fun logParameterChanged(id: Int, type: String) {
        val bundle = Bundle().apply {
            putString("param_id", id.toString())
            putString("param_type", type)
        }
        analytics.logEvent("parameter_changed", bundle)
    }

    fun logButtonClicked(id: String) {
        val bundle = Bundle().apply {
            putString("button_id", id)
        }
        analytics.logEvent("button_clicked", bundle)
    }

    fun logEditText(id: String) {
        val bundle = Bundle().apply {
            putString("edit_id", id)
        }
        analytics.logEvent("button_clicked", bundle)
    }

    fun setUserProperty(key: String, value: String) {
        analytics.setUserProperty(key, value)
    }

    fun setUserId(userId: String) {
        analytics.setUserId(userId)
    }

    fun log(event: String, params: Bundle? = null) {
        analytics.logEvent(event, params)
    }
}
