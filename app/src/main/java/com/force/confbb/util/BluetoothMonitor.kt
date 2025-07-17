package com.force.confbb.util

import kotlinx.coroutines.flow.Flow

interface BluetoothMonitor {
    val isEnabled: Flow<Boolean>
}
