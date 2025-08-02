package com.force.connection.connection

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import com.force.connection.CONN_TAG
import com.force.connection.ConnectionDefaults.log
import com.force.model.ConfException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

class BluetoothDeviceConnection @AssistedInject constructor(
    @Assisted private val deviceAddress: String,
    @Assisted private val scope: CoroutineScope,
    @Assisted override val dataReaderWriter: DataReaderWriter,
    private val bluetoothManager: BluetoothManager,
) : AbstractDeviceConnection(scope) {

    private lateinit var btSocket: BluetoothSocket
    override lateinit var socket: SocketIO

    @SuppressLint("MissingPermission")
    override val info = flowOf(
        DeviceConnection.Info(
            type = DeviceConnection.Type.Bluetooth,
            address = deviceAddress,
            name = bluetoothManager.adapter.getRemoteDevice(deviceAddress).name ?: deviceAddress
        )
    )

    override fun connect() {
        if (::btSocket.isInitialized) {
            log(CONN_TAG, "Already connected to $deviceAddress, disconnect")
            release()
        }
        val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        val bluetoothDevice = bluetoothManager.adapter.getRemoteDevice(deviceAddress)
        btSocket = bluetoothDevice.createRfcommSocketToServiceRecord(sppUuid)
        btSocket.connect()
        socket = ConnectionSocketIO(
            btSocket.inputStream,
            btSocket.outputStream
        )
        log(CONN_TAG, "Connected to $deviceAddress")
    }

    override fun release() {
        runCatching { btSocket.close() }
        super.release()
    }

    @AssistedFactory
    interface Factory {
        fun create(
            deviceAddress: String,
            scope: CoroutineScope,
            dataReaderWriter: DataReaderWriter
        ): BluetoothDeviceConnection
    }
}
