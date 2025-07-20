package com.force.confbb.data

import android.bluetooth.BluetoothManager
import android.util.Log
import com.force.confbb.di.ApplicationScope
import com.force.confbb.model.DeviceConnectionStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.InputStream

class CipherBluetoothDeviceConnectionRepository @AssistedInject constructor(
    @Assisted private val deviceAddress: String,
    @ApplicationScope private val coroutineScope: CoroutineScope,
    bluetoothManager: BluetoothManager
) : BluetoothDeviceConnectionRepository(
    deviceAddress = deviceAddress,
    coroutineScope = coroutineScope,
    bluetoothManager = bluetoothManager
) {
    private val _cipherStatusDate = MutableStateFlow<DeviceConnectionStatus>(DeviceConnectionStatus.Disconnected)

    private val collectingJob = coroutineScope.launch {
        super.data.collect {
            _cipherStatusDate.value = it
            when {
                (it is DeviceConnectionStatus.Connected) -> {
                    val guardText = "guard\n"
                    val handshakeText = "HANDSHAKE"
                    coroutineScope.launch {
                        super.send(guardText.toByteArray())
                        send(handshakeText.toByteArray())
                    }
                }
            }
        }
    }

    override val data: StateFlow<DeviceConnectionStatus> = _cipherStatusDate

    private lateinit var cryptoManager: CryptoManager

    override suspend fun listenInputStream(input: InputStream, isActive: () -> Boolean) {
        try {
            while (isActive()) {
                input.read().let { b ->
                    if (b < 28) {
                        Log.d(
                            "xxx",
                            "Received error $b, "
                        )
                        _cipherStatusDate.value = DeviceConnectionStatus.Error(Exception("Received error code: $b"))
                    } else {
                        val message = ByteArray(b)
                        repeat(b) { message[it] = input.read().toByte() }
                        Log.d(
                            "xxx",
                            "Received message size: $b, " +
                                    "content: ${message.joinToString(" ") { "%02X".format(it) }}"
                        )

                        val text = String(cryptoManager.decryptData(message))
                        if (text == "HANDSHAKE ANSWER") {
                            _cipherStatusDate.value = DeviceConnectionStatus.DataMessage()
                        }
                        Log.d("xxx", "Received text: $text")
                    }
                }
            }
        } catch (ex: Exception) {
            _cipherStatusDate.value = DeviceConnectionStatus.Error(ex)
        }
    }

    override fun send(data: ByteArray) {
        if (!::cryptoManager.isInitialized) {
            cryptoManager = CryptoManager("PiroJOKE")
        }
        val (iv, encrypted) = cryptoManager.encryptData(data)
        val ivEncData = iv + encrypted
        val size = ivEncData.size
        val sizeByteArray = byteArrayOf(
            (size and 0xFF).toByte()
        )
        val dataToSend = sizeByteArray + ivEncData
        Log.d(
            "xxx",
            "Sending message size: $size, " +
                    "content: ${ivEncData.joinToString(" ") { "%02X".format(it) }}"
        )
        super.send(dataToSend)
    }

    override fun close() {
        collectingJob.cancel()
    }

    @AssistedFactory
    interface Factory {
        fun create(deviceAddress: String): CipherBluetoothDeviceConnectionRepository
    }
}
