package com.force.confbb.data.test

import android.util.Log
import com.force.confbb.data.SavedDevicesRepository
import com.force.confbb.model.Device
import com.force.misc.TAG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSavedDeviceRepository @Inject constructor() : SavedDevicesRepository {

    private val mutex = Mutex()

    private val _devices = MutableStateFlow(generateFakeDevices())
    override val devices: Flow<List<Device>> get() = _devices.asStateFlow()

    override suspend fun changePassphrase(device: Device, newPassphrase: String) {
        Log.d(TAG, "Change passphrase for ${device.address} to $newPassphrase")
    }

    override suspend fun getDevice(id: String): Device? {
        return null;
    }

    override suspend fun addDevice(device: Device) {

    }

    override suspend fun setLastSeen(id: String, lastSeen: Long) {

    }

    override suspend fun setName(id: String, name: String) {

    }

    override suspend fun deleteDevice(id: String) {
    }

    private fun generateFakeDevices(): List<Device> {
        return List(30) { index ->
            Device(
                name = "ESP32 Device #$index",
                address = "AA:BB:CC:DD:EE:${index.toString().padStart(2, '0')}",
                lastSeen = if (index % 3 != 0) System.currentTimeMillis() else 0L
            )
        }
    }
}
