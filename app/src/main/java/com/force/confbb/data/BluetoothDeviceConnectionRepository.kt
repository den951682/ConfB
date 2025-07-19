package com.force.confbb.data

import android.bluetooth.BluetoothManager
import com.force.confbb.di.ApplicationScope
import com.force.confbb.model.DeviceConnectionStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch


class BluetoothDeviceConnectionRepository @AssistedInject constructor(
    @Assisted private val deviceAddress: String,
    @ApplicationScope private val coroutineScope: CoroutineScope,
    bluetoothManager: BluetoothManager
) : DeviceConnectionRepository {
    private val bluetoothDevice = bluetoothManager.adapter.getRemoteDevice(deviceAddress)

    private val bluetoothConnectionManager: BluetoothConnectionManager? = if (bluetoothDevice != null) {
        BluetoothConnectionManager(
            bluetoothDevice
        )
    } else {
        null
    }

    override val data: SharedFlow<DeviceConnectionStatus>
        get() = if (bluetoothDevice == null || bluetoothConnectionManager == null) {
            MutableSharedFlow<DeviceConnectionStatus>().also {
                coroutineScope.launch {
                    it.tryEmit(
                        DeviceConnectionStatus.Error(
                            IllegalStateException("Bluetooth device not found for address: $deviceAddress")
                        )
                    )
                }
            }
        } else {
            bluetoothConnectionManager.data
        }

    override fun send(data: ByteArray) {
        bluetoothConnectionManager?.send(data)
    }

    override fun close(exception: Throwable?) {
        bluetoothConnectionManager?.close()
    }

    @AssistedFactory
    interface Factory {
        fun create(deviceAddress: String): BluetoothDeviceConnectionRepository
    }
}
