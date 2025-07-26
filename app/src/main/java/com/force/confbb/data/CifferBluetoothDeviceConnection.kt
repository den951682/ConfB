package com.force.confbb.data

import android.bluetooth.BluetoothManager
import android.util.Log
import com.force.confb.pmodel.BooleanParameter
import com.force.confb.pmodel.FloatParameter
import com.force.confb.pmodel.HandshakeRequest
import com.force.confb.pmodel.HandshakeResponse
import com.force.confb.pmodel.IntParameter
import com.force.confb.pmodel.Message
import com.force.confb.pmodel.ParameterInfo
import com.force.confb.pmodel.StringParameter
import com.force.confbb.model.ConfError
import com.force.confbb.model.DataType
import com.force.confbb.model.DeviceConnectionStatus
import com.force.confbb.util.TAG
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

@Suppress("IMPLICIT_CAST_TO_ANY")
class CipherBluetoothDeviceConnection @AssistedInject constructor(
    @Assisted("address") private val deviceAddress: String,
    @Assisted("pass") private val passPhrase: String,
    @Assisted private val coroutineScope: CoroutineScope,
    bluetoothManager: BluetoothManager
) : BluetoothDeviceConnection(
    deviceAddress = deviceAddress,
    scope = coroutineScope,
    bluetoothManager = bluetoothManager
) {
    private lateinit var cryptoManager: CryptoManager

    init {
        Log.d(TAG, "Initializing CipherBluetoothDeviceConnection for device: $deviceAddress")
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                super.data.collect {
                    when {
                        (it is DeviceConnectionStatus.Connected) -> {
                            val guardText = "guard\n"
                            val request = HandshakeRequest.newBuilder().setText("HANDSHAKE").build().toByteArray()
                            super.send(guardText.toByteArray())
                            Log.d(TAG, "Sending handshake")
                            send(byteArrayOf(DataType.HandshakeRequest.code) + request)
                        }
                    }
                }
            }
        }
    }

    override suspend fun listenInputStream(input: InputStream, isActive: () -> Boolean) {
        try {
            while (!::cryptoManager.isInitialized) {
                delay(100)
            }
            while (isActive()) {
                if (input.available() > 0) {
                    input.read().let { b ->
                        if (b < 28) {
                            val error = ConfError.fromCode(b)
                            if (error.isCritical) {
                                throw error
                            } else {
                                Log.e(TAG, "Received error code: $b, message: ${error.message}")
                                _data.emit(DeviceConnectionStatus.Error(error))
                            }
                        } else {
                            Log.d(TAG, "Frame size: $b")
                            val message = ByteArray(b)
                            repeat(b) {
                                while (input.available() <= 0 && isActive()) {
                                    delay(10)
                                }
                                message[it] = input.read().toByte()
                            }
                            Log.d(
                                TAG,
                                "Received message size: $b, " +
                                        "content: ${message.joinToString(" ") { "%02X".format(it) }}"
                            )

                            val data = try {
                                cryptoManager.decryptData(message)
                            } catch (ex: Exception) {
                                Log.d(TAG, "Error while decrypting data", ex)
                                _data.emit(DeviceConnectionStatus.Error(ConfError.DecryptError()))
                                return@let emptyList<Byte>()
                            }

                            val dataType = DataType.fromCode(data[0])
                            val dataToParse = data.drop(1).toByteArray()
                            val proto = when (dataType) {
                                is DataType.HandshakeResponse -> HandshakeResponse.parseFrom(dataToParse)
                                is DataType.ParameterInfo -> ParameterInfo.parseFrom(dataToParse)
                                is DataType.TypeInt -> IntParameter.parseFrom(dataToParse)
                                is DataType.TypeFloat -> FloatParameter.parseFrom(dataToParse)
                                is DataType.TypeString -> StringParameter.parseFrom(dataToParse)
                                is DataType.TypeBoolean -> BooleanParameter.parseFrom(dataToParse)
                                is DataType.TypeMessage -> Message.parseFrom(dataToParse)

                                else -> {
                                    Log.d(TAG, "Unhandled received data type: ${data[0]}")
                                    _data.emit(DeviceConnectionStatus.Error(ConfError.NotSupportedError()))
                                }
                            }
                            Log.d(TAG, "Received data type: $dataType, content: $proto")
                            _data.tryEmit(DeviceConnectionStatus.DataMessage(proto))
                        }
                    }
                } else {
                    delay(10)
                }
            }
            Log.e(TAG, "End of listen input stream reached")
        } catch (ex: Exception) {
            if (ex !is CancellationException) {
                Log.e(TAG, "Error while reading input stream", ex)
                _data.emit(DeviceConnectionStatus.Error(ex))
            }
        }
    }

    override fun send(data: ByteArray) {
        coroutineScope.launch {
            val dataToSend = withContext(Dispatchers.Default) {
                if (!::cryptoManager.isInitialized) {
                    cryptoManager = CryptoManager(passPhrase)
                }
                val (iv, encrypted) = cryptoManager.encryptData(data)
                val ivEncData = iv + encrypted
                val size = ivEncData.size
                val sizeByteArray = byteArrayOf(
                    (size and 0xFF).toByte()
                )
                Log.d(
                    TAG,
                    "Sending message size: $size, " +
                            "content: ${ivEncData.joinToString(" ") { "%02X".format(it) }}"
                )
                sizeByteArray + ivEncData
            }
            withContext(Dispatchers.IO) {
                super.send(dataToSend)
            }
        }
    }

    override fun close() {
        super.close(null)
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("address") deviceAddress: String,
            @Assisted("pass") passPhrase: String,
            scope: CoroutineScope
        ): CipherBluetoothDeviceConnection
    }
}
