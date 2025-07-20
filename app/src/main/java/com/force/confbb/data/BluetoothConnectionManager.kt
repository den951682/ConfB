package com.force.confbb.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.force.confbb.model.DeviceConnectionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.UUID

open class BluetoothConnectionManager(
    private val device: BluetoothDevice
) : DeviceConnectionRepository {
    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var socket: BluetoothSocket? = null
    private var input: InputStream? = null
    private var output: OutputStream? = null

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _data = MutableSharedFlow<DeviceConnectionStatus>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val data: SharedFlow<DeviceConnectionStatus> = _data.asSharedFlow()

    init {
        scope.launch { _data.emit(DeviceConnectionStatus.Disconnected) }
        connect()
    }


    @SuppressLint("MissingPermission")
    private fun connect() {
        scope.launch {
            try {
                socket = device.createRfcommSocketToServiceRecord(sppUuid)
                socket?.connect()
                input = socket?.inputStream
                output = socket?.outputStream
                _data.emit(DeviceConnectionStatus.Connected(device.name ?: "Unknown Device"))
                listenForIncomingData()
            } catch (e: Exception) {
                close(e)
            }
        }
    }

    override suspend fun listenInputStream(input: InputStream, isActive: () -> Boolean) {
        try {
            BufferedReader(InputStreamReader(input)).use { reader ->
                var line = ""
                while (reader.readLine().also { line = it } != null && isActive()) {
                    _data.emit(DeviceConnectionStatus.Message(line.toByteArray()))
                }
            }
        } catch (e: Exception) {
            close(e)
        }
    }

    override fun send(data: ByteArray) {
        scope.launch {
            try {
                output?.write(data)
                output?.flush()
                _data.emit(DeviceConnectionStatus.SendMessage(data))
            } catch (e: Exception) {
                close(e)
            }
        }
    }

    private fun listenForIncomingData() {
        scope.launch {
            listenInputStream(input!!, { isActive })
        }
    }

    override fun close(exception: Throwable?) {
        runCatching { input?.close() }
        runCatching { output?.close() }
        runCatching { socket?.close() }
        input = null
        output = null
        socket = null
        if (exception != null) {
            scope.launch { _data.emit(DeviceConnectionStatus.Error(exception)) }
        } else {
            scope.launch { _data.emit(DeviceConnectionStatus.Disconnected) }
        }
    }
}
