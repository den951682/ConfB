package com.force.confbb.data

import com.force.confbb.model.Device
import com.force.confbb.model.ScanDevicesStatus
import kotlinx.coroutines.flow.Flow

interface DevicesRepository {
    val status: Flow<ScanDevicesStatus>
    val devices: Flow<List<Device>>
    fun startScan()
    fun stopScan()
}
