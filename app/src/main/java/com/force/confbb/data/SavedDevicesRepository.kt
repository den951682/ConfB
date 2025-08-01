package com.force.confbb.data

import com.force.model.Device
import kotlinx.coroutines.flow.Flow

interface SavedDevicesRepository {
    val devices: Flow<List<Device>>
    suspend fun getDevice(id: String): Device?
    suspend fun addDevice(device: Device)
    suspend fun changePassphrase(device: Device, newPassphrase: String)
    suspend fun setLastSeen(id: String, lastSeen: Long)
    suspend fun setName(id: String, name: String)
    suspend fun deleteDevice(id: String)
}
