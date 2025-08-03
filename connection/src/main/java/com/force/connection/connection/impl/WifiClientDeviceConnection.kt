package com.force.connection.connection.impl

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
import java.net.Socket

class WifiClientDeviceConnection @AssistedInject constructor(
    @Assisted private val host: String,
    @Assisted scope: CoroutineScope,
    @Assisted override val protocol: Protocol
) : AbstractDeviceConnection(scope) {

    private lateinit var tcpSocket: Socket
    override lateinit var socket: SocketIO

    override val info = flowOf(
        DeviceConnection.Info(
            type = DeviceConnection.Type.WifiClient,
            address = "$host:${WifiServerDeviceConnection.DEFAULT_PORT}",
            name = "WifiClient"
        )
    )

    override fun connect() {
        tcpSocket = Socket(host, WifiServerDeviceConnection.DEFAULT_PORT)
        this.socket = ConnectionSocketIO(tcpSocket.getInputStream(), tcpSocket.getOutputStream())
    }

    override fun release() {
        runCatching { tcpSocket.close() }
        super.release()
    }


    @AssistedFactory
    interface Factory {
        fun create(
            host: String,
            scope: CoroutineScope,
            protocol: Protocol
        ): WifiClientDeviceConnection
    }
}
