package com.force.confbb.data

import android.bluetooth.BluetoothManager
import android.util.Log
import com.force.confb.pmodel.HandshakeRequest
import com.force.confb.pmodel.HandshakeResponse
import com.force.confbb.di.ApplicationScope
import com.force.confbb.model.ConfError
import com.force.confbb.model.DataType
import com.force.confbb.model.DeviceConnectionStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.io.InputStream

@Suppress("IMPLICIT_CAST_TO_ANY")
class CipherBluetoothDeviceConnectionRepository @AssistedInject constructor(
    @Assisted private val deviceAddress: String,
    @ApplicationScope private val coroutineScope: CoroutineScope,
    bluetoothManager: BluetoothManager
) : BluetoothDeviceConnectionRepository(
    deviceAddress = deviceAddress,
    coroutineScope = coroutineScope,
    bluetoothManager = bluetoothManager
) {
    private val _cipherStatusDate = MutableSharedFlow<DeviceConnectionStatus>(
        replay = 1,
        extraBufferCapacity = 1
    )

    private val collectingJob = coroutineScope.launch {
        super.data.collect {
            _cipherStatusDate.emit(it)
            when {
                (it is DeviceConnectionStatus.Connected) -> {
                    val guardText = "guard\n"
                    val request = HandshakeRequest.newBuilder().setText("HANDSHAKE").build().toByteArray()
                    coroutineScope.launch {
                        super.send(guardText.toByteArray())
                        send(byteArrayOf(0) + request)
                    }
                }
            }
        }
    }

    override val data: SharedFlow<DeviceConnectionStatus> = _cipherStatusDate

    private lateinit var cryptoManager: CryptoManager


    override suspend fun listenInputStream(input: InputStream, isActive: () -> Boolean) {
        try {
            while (isActive()) {
                input.read().let { b ->
                    if (b < 28) {
                        val error = ConfError.fromCode(b)
                        Log.d("xxx", "Received error ${error.message}")
                        throw error
                    } else {
                        val message = ByteArray(b)
                        repeat(b) { message[it] = input.read().toByte() }
                        Log.d(
                            "xxx",
                            "Received message size: $b, " +
                                    "content: ${message.joinToString(" ") { "%02X".format(it) }}"
                        )
                        val data = cryptoManager.decryptData(message)
                        val dataType = DataType.fromCode(data[0])
                        val dataToParse = data.drop(1).toByteArray()
                        val proto = when (dataType) {
                            is DataType.HandshakeResponse -> HandshakeResponse.parseFrom(dataToParse)
                            else -> {
                                Log.d("xxx", "Unhandled received data type: ${data[0]}")
                                Unit
                            }
                        }
                        _cipherStatusDate.emit(DeviceConnectionStatus.DataMessage(proto))
                    }
                }

            }
        } catch (ex: Exception) {
            _cipherStatusDate.emit(DeviceConnectionStatus.Error(ex))
        }
    }

    override fun send(data: ByteArray) {
        if (!::cryptoManager.isInitialized) {
            cryptoManager = CryptoManager("PiroJOKE1")
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
