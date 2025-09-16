package com.force.confbb.data

import com.force.model.Device
import kotlinx.coroutines.flow.Flow

interface SavedDevicesRepository {
    val devices: Flow<List<Device>>
    suspend fun getDevice(id: String, loraAddress: Int): Device?
    suspend fun addDevice(device: Device)
    suspend fun changePassphrase(device: Device, newPassphrase: String)
    suspend fun changeProtocol(device: Device, protocol: Device.Protocol)
    suspend fun setLastSeen(id: String, loraAddress: Int, lastSeen: Long)
    suspend fun setName(id: String, loraAddress: Int, name: String)
    suspend fun deleteDevice(id: String, loraAddress: Int)
}
