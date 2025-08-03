package com.force.connection.connection.impl

import android.annotation.SuppressLint
import android.net.wifi.WifiManager
import com.force.connection.InetAddressHelper
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
import kotlinx.coroutines.launch
import java.net.ServerSocket
import java.net.Socket

class WifiServerDeviceConnection @AssistedInject constructor(
    @Assisted private val scope: CoroutineScope,
    @Assisted override val protocol: Protocol,
    private val inetAddressHelper: InetAddressHelper,
    wifiManager: WifiManager,
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
            address = "${wifiManager.connectionInfo.ipAddress.asIp()}:$DEFAULT_PORT",
            name = "WifiServer"
        )
    )

    override suspend fun connect() {
        inetAddressHelper.init()
        inetAddressHelper.startBroadcast()
        serverSocket = ServerSocket(DEFAULT_PORT)
        clientSocket = serverSocket.accept()
        socket = ConnectionSocketIO(clientSocket.getInputStream(), clientSocket.getOutputStream())
    }

    override fun release() {
        runCatching { clientSocket.close() }
        runCatching { serverSocket.close() }
        scope.launch {
            runCatching {
                inetAddressHelper.stopBroadcast()
                inetAddressHelper.release()
            }
        }
        super.release()
    }

    @SuppressLint("DefaultLocale")
    fun Int.asIp() = String.format(
        "%d.%d.%d.%d",
        this and 0xff,
        this shr 8 and 0xff,
        this shr 16 and 0xff,
        this shr 24 and 0xff
    )

    @AssistedFactory
    interface Factory {
        fun create(
            scope: CoroutineScope,
            protocol: Protocol
        ): WifiServerDeviceConnection
    }
}
