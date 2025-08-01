package com.force.connection.connection

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import com.force.connection.CONN_TAG
import com.force.connection.ConnectionDefaults.log
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

@SuppressLint("MissingPermission")
class BluetoothDeviceConnection @AssistedInject constructor(
    @Assisted private val deviceAddress: String,
    @Assisted private val scope: CoroutineScope,
    @Assisted override val dataReaderWriter: DataReaderWriter,
    private val bluetoothManager: BluetoothManager,
) : AbstractDeviceConnection(scope) {
    private lateinit var socket: BluetoothSocket
    override lateinit var input: InputStream
    override lateinit var output: OutputStream

    override val info: DeviceConnection.Info by lazy {
        DeviceConnection.Info(
            type = DeviceConnection.Type.Bluetooth,
            address = deviceAddress,
            name = bluetoothManager.adapter.getRemoteDevice(deviceAddress).name ?: deviceAddress
        )
    }

    override fun connect() {
        val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        val bluetoothDevice = bluetoothManager.adapter.getRemoteDevice(deviceAddress)
        socket = bluetoothDevice.createRfcommSocketToServiceRecord(sppUuid)
        socket.connect()
        log(CONN_TAG, "Connected to $deviceAddress")
        input = socket.inputStream
        output = socket.outputStream
    }

    override fun release() {
        runCatching { input.close() }
        runCatching { output.close() }
        runCatching { socket.close() }
        super.release()
    }

    @AssistedFactory
    interface Factory {
        fun create(
            deviceAddress: String,
            scope: CoroutineScope,
            dataReader: DataReaderWriter
        ): BluetoothDeviceConnection
    }
}