package com.force.confbb.data

import com.force.confbb.model.Device
import kotlinx.coroutines.flow.Flow

interface SavedDevicesRepository {
    val devices: Flow<List<Device>>
    suspend fun changePassphrase(device: Device, newPassphrase: String)
    suspend fun deleteDevice(device: Device)
}
