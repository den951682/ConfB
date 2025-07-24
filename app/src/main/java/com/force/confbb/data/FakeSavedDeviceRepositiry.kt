package com.force.confbb.data

import android.util.Log
import com.force.confbb.model.Device
import com.force.confbb.util.TAG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class FakeSavedDeviceRepository @Inject constructor() : SavedDevicesRepository {

    private val mutex = Mutex()

    private val _devices = MutableStateFlow(generateFakeDevices())
    override val devices: Flow<List<Device>> get() = _devices.asStateFlow()

    override suspend fun changePassphrase(device: Device, newPassphrase: String) {
        Log.d(TAG, "Change passphrase for ${device.address} to $newPassphrase")
    }

    override suspend fun deleteDevice(device: Device) {
        mutex.withLock {
            _devices.value = _devices.value.filterNot { it.address == device.address }
        }
    }

    private fun generateFakeDevices(): List<Device> {
        return List(30) { index ->
            Device(
                name = "ESP32 Device #$index",
                address = "AA:BB:CC:DD:EE:${index.toString().padStart(2, '0')}",
                isAvailable = index % 3 != 0
            )
        }
    }
}
