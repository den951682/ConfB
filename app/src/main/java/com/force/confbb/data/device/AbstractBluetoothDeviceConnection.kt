package com.force.confbb.data.device

import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.force.confbb.util.TAG
import kotlinx.coroutines.CoroutineScope
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

abstract class AbstractBluetoothDeviceConnection(
    private val deviceAddress: String,
    scope: CoroutineScope,
    bluetoothManager: BluetoothManager,
) : AbstractDeviceConnection(scope) {
    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val bluetoothDevice = bluetoothManager.adapter.getRemoteDevice(deviceAddress)

    private lateinit var socket: BluetoothSocket
    override lateinit var input: InputStream
    override lateinit var output: OutputStream

    override fun connect() {
        socket = bluetoothDevice.createRfcommSocketToServiceRecord(sppUuid)
        socket.connect()
        Log.d(TAG, "Connected to $deviceAddress")
        input = socket.inputStream
        output = socket.outputStream
    }

    override fun release() {
        runCatching { input.close() }
        runCatching { output.close() }
        runCatching { socket.close() }
        super.release()
    }
}
