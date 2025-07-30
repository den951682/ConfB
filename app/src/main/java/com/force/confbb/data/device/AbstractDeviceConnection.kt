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
    scope: CoroutineScope,
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

    private val inputError = PipedInputStream()

    protected val errorOutput = PipedOutputStream(inputError)

    protected abstract val input: InputStream

    protected abstract val output: OutputStream

    init {
        scope.launch(Dispatchers.IO) {
            try {
                connect()
                _state.value = DeviceConnection.State.Connected
                while (isActive) {
                    readDataObject()
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
                _events.tryEmit(DeviceConnection.Event.Error(fromCode(inputError.read())))
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

    override fun close() {
        release()
        _state.value = DeviceConnection.State.Disconnected
    }

    abstract fun readDataObject(): Any

    abstract fun connect()

    protected open fun release() {
        try {
            inputError.close()
            errorOutput.close()
        } catch (e: Exception) {
        }
    }
}
