package com.force.confbb.data

import android.bluetooth.BluetoothManager
import com.force.confbb.di.ApplicationScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope


open class BluetoothDeviceConnectionRepository @AssistedInject constructor(
    @Assisted private val deviceAddress: String,
    @ApplicationScope private val coroutineScope: CoroutineScope,
    bluetoothManager: BluetoothManager
) : BluetoothConnectionManager(bluetoothManager.adapter.getRemoteDevice(deviceAddress)) {
    private val bluetoothDevice = bluetoothManager.adapter.getRemoteDevice(deviceAddress)

    /*
    override val data: Flow<DeviceConnectionStatus>
        get() = if (bluetoothDevice == null || bluetoothConnectionManager == null) {
            flowOf(
                DeviceConnectionStatus.Error(
                    IllegalStateException("Bluetooth device not found for address: $deviceAddress")
                )
            )
        } else {
            bluetoothConnectionManager.data
        }
    */

    @AssistedFactory
    interface Factory {
        fun create(deviceAddress: String): BluetoothDeviceConnectionRepository
    }
}
