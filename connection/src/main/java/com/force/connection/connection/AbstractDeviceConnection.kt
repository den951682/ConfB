package com.force.connection.connection

import com.force.connection.CONN_TAG
import com.force.connection.ConnectionDefaults.log
import com.force.connection.ConnectionEvent
import com.force.connection.protocol.Protocol
import com.force.model.ConfException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class AbstractDeviceConnection(
    private val scope: CoroutineScope,
) : DeviceConnection {
    @Volatile
    private var started = false

    private val lifecycle: ConnectionLifecycleManager = ConnectionLifecycleManager()

    private val _dataObjects = MutableSharedFlow<Any>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    override val dataObjects = _dataObjects

    override val state = lifecycle.state

    protected val _events = MutableSharedFlow<ConnectionEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val events = _events

    protected abstract val socket: SocketIO

    protected abstract val protocol: Protocol

    override fun start() {
        if (!started) {
            started = true
            scope.launch {
                setupConnection()
                runCatching { EventObserver(scope, socket.eventsInput, _events::emit).start() }
            }
        }
    }

    private suspend fun setupConnection() = withContext(Dispatchers.IO) {
        try {
            connect()
            protocol.init(socket.input, socket.output)
            lifecycle.transitionTo(DeviceConnection.State.Connected)
            ReaderLoop(protocol, _dataObjects::emit, lifecycle::handleError).start()
        } catch (ex: Exception) {
            lifecycle.handleError(ex)
        } finally {
            release()
        }
    }

    abstract fun connect()

    protected fun send(data: ByteArray) {
        try {
            socket.output.write(data)
            socket.output.flush()
            log(CONN_TAG, "Sent data, size: ${data.size}")
        } catch (ex: Exception) {
            lifecycle.transitionTo(DeviceConnection.State.Error(ConfException.SocketException()))
            release()
        }
    }

    override fun sendDataObject(dataObject: Any) {
        scope.launch(Dispatchers.IO) {
            try {
                protocol.send(dataObject)
                log(CONN_TAG, "Sent data object: ${dataObject::class.simpleName}")
            } catch (ex: Exception) {
                lifecycle.handleError(ex)
            }
        }
    }

    override fun close() {
        release()
        lifecycle.transitionTo(DeviceConnection.State.Disconnected)
    }

    protected open fun release() {
        try {
            log(CONN_TAG, "Releasing connection")
            socket.close()
        } catch (ex: Exception) {
            log(CONN_TAG, "Failed to close socket: ${ex.message}")
        }
    }
}
