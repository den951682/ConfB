package com.force.connection.connection.impl

import android.net.wifi.WifiManager
import com.force.connection.connection.AbstractDeviceConnection
import com.force.connection.connection.ConnectionSocketIO
import com.force.connection.connection.DeviceConnection
import com.force.connection.connection.SocketIO
import com.force.connection.protocol.Protocol
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import java.net.ServerSocket
import java.net.Socket

class WifiServerDeviceConnection @AssistedInject constructor(
    @Assisted scope: CoroutineScope,
    @Assisted override val protocol: Protocol,
    wifiManager: WifiManager
) : AbstractDeviceConnection(scope) {
    companion object {
        const val DEFAULT_PORT = 49169
    }

    private lateinit var serverSocket: ServerSocket
    private lateinit var clientSocket: Socket

    override lateinit var socket: SocketIO

    override val info = flowOf(
        DeviceConnection.Info(
            type = DeviceConnection.Type.WifiServer,
            //todo use modern api to get IP address
            address = "${wifiManager.connectionInfo.ipAddress}:$DEFAULT_PORT",
            name = "WifiServer"
        )
    )

    override fun connect() {
        serverSocket = ServerSocket(DEFAULT_PORT)
        clientSocket = serverSocket.accept()
        socket = ConnectionSocketIO(clientSocket.getInputStream(), clientSocket.getOutputStream())
    }

    override fun release() {
        runCatching { clientSocket.close() }
        runCatching { serverSocket.close() }
        super.release()
    }

    @AssistedFactory
    interface Factory {
        fun create(
            scope: CoroutineScope,
            protocol: Protocol
        ): WifiServerDeviceConnection
    }
}
