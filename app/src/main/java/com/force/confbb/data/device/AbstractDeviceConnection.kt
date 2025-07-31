package com.force.confbb.data.device

import android.util.Log
import com.force.confbb.model.ConfError
import com.force.confbb.model.ConfError.Companion.fromCode
import com.force.confbb.util.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlin.coroutines.cancellation.CancellationException

abstract class AbstractDeviceConnection(
    private val scope: CoroutineScope,
) : DeviceConnection {
    private val _dataObjects = MutableSharedFlow<Any>(
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val dataObjects = _dataObjects

    protected val _state = MutableStateFlow<DeviceConnection.State>(DeviceConnection.State.Connecting)

    override val state = _state

    protected val _events = MutableSharedFlow<DeviceConnection.Event>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val events = _events

    private val inputEvents = PipedInputStream()

    protected val eventsOutput = PipedOutputStream(inputEvents)

    protected abstract val input: InputStream

    protected abstract val output: OutputStream

    protected abstract val dataReaderWriter: DataReaderWriter

    override fun start() {
        scope.launch(Dispatchers.IO) {
            try {
                connect()
                dataReaderWriter.init(input, output, eventsOutput, this@AbstractDeviceConnection::send)
                _state.value = DeviceConnection.State.Connected
                while (isActive) {
                    dataObjects.tryEmit(dataReaderWriter.readDataObject())
                }
                _state.value = DeviceConnection.State.Disconnected
            } catch (ex: Exception) {
                if (ex is CancellationException) {
                    _state.value = DeviceConnection.State.Disconnected
                } else {
                    val error = when (ex) {
                        is IOException -> ConfError.SocketError()
                        is ConfError -> ex
                        else -> ConfError.UnknownError(ex.message ?: "")
                    }
                    _state.value = DeviceConnection.State.Error(error)
                }
            } finally {
                release()
            }
        }
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                runCatching { _events.tryEmit(DeviceConnection.Event.Error(fromCode(inputEvents.read()))) }
            }
        }
    }

    protected fun send(data: ByteArray) {
        try {
            output.write(data)
            output.flush()
            Log.d(TAG, "Sent data, size: ${data.size}")
        } catch (ex: Exception) {
            _state.value = DeviceConnection.State.Error(ConfError.SocketError())
            release()
        }
    }

    override fun sendDataObject(dataObject: Any) {
        try {
            dataReaderWriter.sendDataObject(dataObject)
            Log.d(TAG, "Sent data object: ${dataObject::class.simpleName}")
        } catch (ex: Exception) {
            if (ex is CancellationException) {
                _state.value = DeviceConnection.State.Disconnected
            } else {
                val error = when (ex) {
                    is IOException -> ConfError.SocketError()
                    is ConfError -> ex
                    else -> ConfError.UnknownError(ex.message ?: "")
                }
                _state.value = DeviceConnection.State.Error(error)
            }
        }
    }

    override fun close() {
        release()
        _state.value = DeviceConnection.State.Disconnected
    }

    abstract fun connect()

    protected open fun release() {
        try {
            inputEvents.close()
            eventsOutput.close()
        } catch (e: Exception) {
        }
    }

    interface DataReaderWriter {
        fun init(input: InputStream, outputStream: OutputStream, eventStream: OutputStream, send: (ByteArray) -> Unit)
        fun readDataObject(): Any
        fun sendDataObject(dataObject: Any)
    }
}
