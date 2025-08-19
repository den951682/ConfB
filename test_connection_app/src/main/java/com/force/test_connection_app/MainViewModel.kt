package com.force.test_connection_app

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.force.connection.connection.DeviceConnection
import com.force.connection.connection.impl.BluetoothClientDeviceConnection
import com.force.connection.connection.impl.BluetoothServerDeviceConnection
import com.force.connection.connection.impl.WifiClientDeviceConnection
import com.force.connection.connection.impl.WifiServerDeviceConnection
import com.force.connection.protocol.EcdhAesProtocol
import com.force.connection.protocol.PassPhraseAesProtocol
import com.force.connection.protocol.ProtocolParser
import com.force.connection.protocol.ProtocolSerializer
import com.force.connection.protocol.RawProtocol
import com.force.crypto.CryptoAes
import com.force.crypto.CryptoEcdh
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
            serializer = object : ProtocolSerializer {
                override fun serialize(data: Any): ByteArray {
                    return data.toString().toByteArray()
                }
            },
            parser = object : ProtocolParser {
                override fun parse(data: ByteArray): String {
                    return String(data)
                }
            },
            cryptoProducer = object : PassPhraseAesProtocol.CryptoProducer {
                private lateinit var crypto: CryptoAes
                override fun init() {
                    crypto = CryptoAes(passphrase = "PASS")
                }

                override fun getDecrypt(): (ByteArray) -> ByteArray = crypto::decryptData

                override fun getEncrypt(): (ByteArray) -> ByteArray = crypto::encryptDataWhole
            },
            header = "HEADER\n".toByteArray()
        )
    }

    val ecdhAesProtocol by always {
        EcdhAesProtocol(
            serializer = object : ProtocolSerializer {
                override fun serialize(data: Any): ByteArray {
                    return data.toString().toByteArray()
                }
            },
            parser = object : ProtocolParser {
                override fun parse(data: ByteArray): String {
                    return String(data)
                }
            },
            cryptoProducer = object : EcdhAesProtocol.CryptoProducer {
                private lateinit var crypto: CryptoEcdh
                override fun init() {
                    crypto = CryptoEcdh()
                }

                override fun getPublic(): ByteArray {
                    return crypto.getPublicKey()
                }

                override fun applyOtherPublic(publicKey: ByteArray) {
                    crypto.applyOtherPublic(publicKey)
                }

                override fun getDecrypt(): (ByteArray) -> ByteArray = crypto::decryptData

                override fun getEncrypt(): (ByteArray) -> ByteArray = crypto::encryptData
            },
            bindPhraseProducer = object : EcdhAesProtocol.BindPhraseProducer {
                override fun getBindPhrase(): String {
                    return "PASS1"
                }

            },
            true,
            header = "HEADER\n".toByteArray()
        )
    }

    val rawProtocol by always {
        RawProtocol(
            serializer = object : ProtocolSerializer {
                override fun serialize(data: Any): ByteArray {
                    return data.toString().toByteArray()
                }
            },
            parser = object : ProtocolParser {
                override fun parse(data: ByteArray): String {
                    return String(data)
                }
            },
            bindPhraseProducer = object : RawProtocol.BindPhraseProducer {
                override fun getBindPhrase(): String {
                    return "phrase"
                }
            },
            true,
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
                rawProtocol
            )
            c.start()
            _connection.emit(c)
            var n = 10
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
                rawProtocol
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
                ecdhAesProtocol
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
                ecdhAesProtocol
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
