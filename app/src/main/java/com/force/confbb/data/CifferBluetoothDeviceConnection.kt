package com.force.confbb.data

import android.bluetooth.BluetoothManager
import android.util.Log
import com.force.confb.pmodel.HandshakeRequest
import com.force.confb.pmodel.HandshakeResponse
import com.force.confb.pmodel.ParameterInfo
import com.force.confbb.model.ConfError
import com.force.confbb.model.DataType
import com.force.confbb.model.DeviceConnectionStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.nio.charset.Charset

@Suppress("IMPLICIT_CAST_TO_ANY")
class CipherBluetoothDeviceConnection @AssistedInject constructor(
    @Assisted private val deviceAddress: String,
    @Assisted private val coroutineScope: CoroutineScope,
    bluetoothManager: BluetoothManager
) : BluetoothDeviceConnection(
    deviceAddress = deviceAddress,
    scope = coroutineScope,
    bluetoothManager = bluetoothManager
) {
   private lateinit var cryptoManager: CryptoManager

    private val collectingJob = coroutineScope.launch {
        withContext(Dispatchers.IO) {
            super.data.collect {
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
    }

    override suspend fun listenInputStream(input: InputStream, isActive: () -> Boolean) {
        try {
            while (isActive()) {
                if (input.available() > 0) {
                    input.read().let { b ->
                        if (b < 28) {
                            val error = ConfError.fromCode(b)
                            Log.d("xxx", "Received error ${error.message}")
                            throw error
                        } else {
                            Log.d("xxx", "Frame size: $b")
                            val message = ByteArray(b)
                            repeat(b) {
                                while (input.available() <= 0 && isActive()) {
                                    delay(10)
                                }
                                message[it] = input.read().toByte().also {
                                    //Log.d("xxx", "Read byte: ${"%02X".format(it)}")
                                }
                            }
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
                                is DataType.ParameterInfo -> ParameterInfo.parseFrom(dataToParse).also {
                                    Log.d("xxx", it.id.toString())
                                    Log.d("xxx", it.name.toString(Charset.forName("UTF-8")))
                                    Log.d("xxx", it.description.toString(Charset.forName("UTF-8")))
                                }

                                else -> {
                                    Log.d("xxx", "Unhandled received data type: ${data[0]}")
                                    Unit
                                }
                            }
                            Log.d("xxx", "Received data type: $dataType, content: $proto")
                            _data.tryEmit(DeviceConnectionStatus.DataMessage(proto))
                        }
                    }
                } else {
                    delay(250)
                }
            }
            Log.e("xxx", "End of listen input stream reached")
        } catch (ex: Exception) {
            Log.e("xxx", "Error while reading input stream", ex)
            _data.emit(DeviceConnectionStatus.Error(ex))
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
        fun create(deviceAddress: String, scope: CoroutineScope): CipherBluetoothDeviceConnection
    }
}
