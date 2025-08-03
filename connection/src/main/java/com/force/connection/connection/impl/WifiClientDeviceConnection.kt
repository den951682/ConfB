package com.force.connection.connection.impl

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.net.Socket

class WifiClientDeviceConnection @AssistedInject constructor(
    @Assisted private val scope: CoroutineScope,
    @Assisted override val protocol: Protocol,
    private val inetAddressHelper: InetAddressHelper
) : AbstractDeviceConnection(scope) {

    private lateinit var tcpSocket: Socket
    override lateinit var socket: SocketIO

    private val _info = MutableStateFlow(
        DeviceConnection.Info(
            type = DeviceConnection.Type.WifiClient,
            address = "",
            name = "WifiClient"
        )
    )

    override val info = _info

    override suspend fun connect() {
        inetAddressHelper.init()
        val host = inetAddressHelper.resolvedAddress.first()
        _info.emit(_info.value.copy(name = "WifiClient for ${host}"))
        tcpSocket = Socket(host, WifiServerDeviceConnection.DEFAULT_PORT)
        this.socket = ConnectionSocketIO(tcpSocket.getInputStream(), tcpSocket.getOutputStream())
    }

    override fun release() {
        runCatching { tcpSocket.close() }
        scope.launch { runCatching { inetAddressHelper.release() } }
        super.release()
    }

    @AssistedFactory
    interface Factory {
        fun create(
            scope: CoroutineScope,
            protocol: Protocol
        ): WifiClientDeviceConnection
    }
}
