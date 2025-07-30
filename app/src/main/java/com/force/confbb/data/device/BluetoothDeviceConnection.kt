package com.force.confbb.data.device

import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.force.confbb.util.TAG
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID


class BluetoothDeviceConnection @AssistedInject constructor(
    @Assisted private val deviceAddress: String,
    @Assisted private val scope: CoroutineScope,
    @Assisted override val dataReaderWriter: DataReaderWriter,
    private val bluetoothManager: BluetoothManager,
) : AbstractDeviceConnection(scope) {
    private lateinit var socket: BluetoothSocket
    override lateinit var input: InputStream
    override lateinit var output: OutputStream

    override fun connect() {
        val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        val bluetoothDevice = bluetoothManager.adapter.getRemoteDevice(deviceAddress)
        socket = bluetoothDevice.createRfcommSocketToServiceRecord(sppUuid)
        socket.connect()
        Log.d(TAG, "Connected to $deviceAddress")
        input = socket.inputStream
        output = socket.outputStream
    }

    override fun sendDataObject(dataObject: Any) {
        scope.launch(Dispatchers.IO) {
            dataReaderWriter.sendDataObject(dataObject)
        }
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
