package com.force.confbb.data

import com.force.model.Device
import com.force.model.ScanDevicesStatus
import kotlinx.coroutines.flow.Flow

interface DevicesRepository {
    val enabled: Flow<Boolean>
    val status: Flow<ScanDevicesStatus>
    val devices: Flow<List<Device>>
    fun startScan()
    fun stopScan()
}
