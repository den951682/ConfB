package com.force.test_connection_app

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.force.connection.connection.DeviceConnection
import com.force.connection.connection.impl.BluetoothClientDeviceConnection
import com.force.connection.connection.impl.BluetoothServerDeviceConnection
import com.force.connection.connection.impl.WifiClientDeviceConnection
import com.force.connection.connection.impl.WifiServerDeviceConnection
import com.force.connection.protocol.PassPhraseAesProtocol
import com.force.connection.protocol.PlainProtocol
import com.force.crypto.CryptoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val wifiServerFabric: WifiServerDeviceConnection.Factory,
    private val wifiClientFabric: WifiClientDeviceConnection.Factory,
    private val btServerFabric: BluetoothServerDeviceConnection.Factory,
    private val btClientFabric: BluetoothClientDeviceConnection.Factory
) : ViewModel() {
    private val _connection = MutableStateFlow<DeviceConnection?>(null)

    val connection: Flow<DeviceConnection?> = _connection
    private var sendJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            connection.filterNotNull().flatMapLatest {
                it.events
            }.filterIsInstance<DeviceConnection.State.Error>()
                .onEach { sendJob?.cancel() }
                .collect()
        }
    }

    val passPhraseAesProtocol by lazy {
        PassPhraseAesProtocol(
            serializer = object : PassPhraseAesProtocol.Serializer {
                override fun serialize(data: Any): ByteArray {
                    return data.toString().toByteArray()
                }
            },
            parser = object : PassPhraseAesProtocol.Parser {
                override fun parse(data: ByteArray): String {
                    return String(data)
                }
            },
            cryptoProducer = object : PassPhraseAesProtocol.CryptoProducer {
                private lateinit var crypto: CryptoManager
                override fun init() {
                    crypto = CryptoManager(passphrase = "PASS")
                }

                override fun getDecrypt(): (ByteArray) -> ByteArray = crypto::decryptData

                override fun getEncrypt(): (ByteArray) -> ByteArray = crypto::encryptDataWhole
            },
            header = "HEADER\n".toByteArray()
        )
    }

    init {
        viewModelScope.launch {
            connection.filterNotNull().flatMapLatest { it.state }
                .onEach { state ->
                    if (state is DeviceConnection.State.Error) {
                        Log.d(TAG, "Connection error: ${state.error.message}")
                    }
                }
                .collect()
        }
    }

    fun startWifiServer() {
        sendJob?.cancel()
        sendJob = viewModelScope.launchCancellable({
            val c = wifiServerFabric.create(
                viewModelScope,
                PlainProtocol()
            )
            c.start()
            _connection.emit(c)
            var n = 0
            while (isActive) {
                try {
                    c.sendDataObject(n++)
                } catch (ex: Exception) {
                    Log.e(TAG, "Error sending data object", ex)
                }
                delay(1000)
            }
        }) {
            _connection.value?.close()
            Log.e(TAG, "Connection closed")
            _connection.emit(null)
        }
    }

    fun startWifiClient() {
        sendJob?.cancel()
        sendJob = viewModelScope.launchCancellable({
            val c = wifiClientFabric.create(
                viewModelScope,
                PlainProtocol()
            )
            c.start()
            _connection.emit(c)
            while (isActive) {
                words.forEach {
                    try {
                        c.sendDataObject(it)
                    } catch (ex: Exception) {
                        Log.e(TAG, "Error sending data object", ex)
                    }
                    delay(500)
                }
            }
        }) {
            _connection.value?.close()
            Log.e(TAG, "Connection closed")
            _connection.emit(null)
        }
    }

    fun startBtServer() {
        sendJob?.cancel()
        sendJob = viewModelScope.launchCancellable({
            val c = btServerFabric.create(
                viewModelScope,
                passPhraseAesProtocol
            )
            c.start()
            _connection.emit(c)
            var n = 0
            while (isActive && _connection.value != null) {
                try {
                    c.sendDataObject(n++)
                } catch (ex: Exception) {
                    Log.e(TAG, "Error sending data object", ex)
                }
                delay(1000)
            }
        }) {
            _connection.value?.close()
            Log.e(TAG, "Connection closed")
            _connection.emit(null)
        }
    }

    fun startBtClient(address: String) {
        sendJob?.cancel()
        sendJob = viewModelScope.launchCancellable({
            val c = btClientFabric.create(
                address,
                viewModelScope,
                passPhraseAesProtocol
            )
            c.start()
            _connection.emit(c)
            while (isActive) {
                words.forEach {
                    if (_connection.value != null) {
                        try {
                            c.sendDataObject(it)
                        } catch (ex: Exception) {
                            Log.e(TAG, "Error sending data object", ex)
                        }
                        delay(500)
                    }
                }
            }
        }) {
            _connection.value?.close()
            Log.e(TAG, "Connection closed")
            _connection.emit(null)
        }
    }

    fun stopConnection() {
        sendJob?.cancel()
    }

    fun CoroutineScope.launchCancellable(
        block: suspend CoroutineScope.() -> Unit,
        onCancel: suspend () -> Unit
    ): Job {
        val job = launch(Dispatchers.IO) {
            try {
                block()
            } finally {
                if (!isActive) onCancel()
            }
        }
        return job
    }
}
