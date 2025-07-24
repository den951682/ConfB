package com.force.confbb.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.annotation.RequiresPermission
import com.force.confbb.model.DeviceConnectionStatus
import com.force.confbb.util.TAG
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.UUID

open class BluetoothDeviceConnection @AssistedInject constructor(
    @Assisted private val deviceAddress: String,
    @Assisted private val scope: CoroutineScope,
    bluetoothManager: BluetoothManager,
) : DeviceConnection {
    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val bluetoothDevice = bluetoothManager.adapter.getRemoteDevice(deviceAddress)

    private var socket: BluetoothSocket? = null
    private var input: InputStream? = null
    private var output: OutputStream? = null

    protected val _data = MutableSharedFlow<DeviceConnectionStatus>(
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val credentials: Pair<String, String>
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        get() = bluetoothDevice.name to bluetoothDevice.address

    override val data: SharedFlow<DeviceConnectionStatus> = _data.asSharedFlow()

    init {
        scope.launch { _data.emit(DeviceConnectionStatus.Disconnected) }
        connect()
    }

    @SuppressLint("MissingPermission")
    private fun connect() {
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    socket = bluetoothDevice.createRfcommSocketToServiceRecord(sppUuid)
                    socket?.connect()
                    Log.d(TAG, "Connected to ${bluetoothDevice.name} at $deviceAddress")
                    input = socket?.inputStream
                    output = socket?.outputStream
                    _data.emit(DeviceConnectionStatus.Connected(bluetoothDevice.name ?: "Unknown Device"))
                    listenForIncomingData()
                } catch (e: Exception) {
                    close(e)
                }
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
            withContext(Dispatchers.IO) {
                try {
                    output?.write(data)
                    output?.flush()
                    _data.emit(DeviceConnectionStatus.SendMessage(data))
                } catch (e: Exception) {
                    close(e)
                }
            }
        }
    }

    private fun listenForIncomingData() {
        scope.launch {
            withContext(Dispatchers.IO) {
                listenInputStream(input!!, { isActive })
            }
            Log.e(TAG, "Listening for incoming data on $deviceAddress ended")
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
        Log.d(TAG, "Connection to $deviceAddress closed, exception: $exception")
    }

    @AssistedFactory
    interface Factory {
        fun create(deviceAddress: String, scope: CoroutineScope): BluetoothDeviceConnection
    }
}
