package com.force.connection.connection.impl

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import androidx.annotation.RequiresPermission
import com.force.connection.CONN_TAG
import com.force.connection.ConnectionDefaults
import com.force.connection.connection.AbstractDeviceConnection
import com.force.connection.connection.ConnectionSocketIO
import com.force.connection.connection.DeviceConnection
import com.force.connection.connection.SocketIO
import com.force.connection.protocol.Protocol
import com.force.connection.protocol.RawProtocol
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

class BluetoothServerDeviceConnection @AssistedInject constructor(
    @Assisted private val scope: CoroutineScope,
    @Assisted override val protocol: Protocol,
    private val bluetoothManager: BluetoothManager,
) : AbstractDeviceConnection(scope) {
    companion object {
        val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private lateinit var btServerSocket: BluetoothServerSocket
    private lateinit var btSocket: BluetoothSocket
    override lateinit var socket: SocketIO

    @SuppressLint("MissingPermission")
    override val info = flowOf(
        DeviceConnection.Info(
            type = DeviceConnection.Type.Bluetooth,
            address = bluetoothManager.adapter.address,
            name = bluetoothManager.adapter.name ?: bluetoothManager.adapter.name,
            isFast = protocol is RawProtocol
        )
    )

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun connect() {
        if (::btSocket.isInitialized) {
            ConnectionDefaults.log(CONN_TAG, "Already server started, disconnect")
            release()
        }
        btServerSocket = bluetoothManager.adapter.listenUsingRfcommWithServiceRecord("Conf", sppUuid)
        btSocket = btServerSocket.accept()
        socket = ConnectionSocketIO(
            btSocket.inputStream,
            btSocket.outputStream
        )
        ConnectionDefaults.log(CONN_TAG, "Server started, connected to ${btSocket.remoteDevice.address}")
    }

    override fun release() {
        runCatching { btSocket.close() }
        runCatching { btServerSocket.close() }
        super.release()
    }

    @AssistedFactory
    interface Factory {
        fun create(
            scope: CoroutineScope,
            protocol: Protocol
        ): BluetoothServerDeviceConnection
    }
}