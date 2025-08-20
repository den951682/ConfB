package com.force.confbb.feature.device

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.force.confbb.data.SavedDevicesRepository
import com.force.confbb.parsing.ConfParser
import com.force.confbb.serialization.ConfSerializer
import com.force.connection.connection.impl.BluetoothClientDeviceConnection
import com.force.connection.device.RemoteDevice
import com.force.connection.device.RemoteDeviceImpl
import com.force.connection.protocol.EcdhAesProtocol
import com.force.connection.protocol.PassPhraseAesProtocol
import com.force.connection.protocol.Protocol
import com.force.connection.protocol.RawProtocol
import com.force.crypto.CryptoAes
import com.force.crypto.CryptoEcdh
import com.force.misc.PASS_PHRASE
import com.force.misc.TAG
import com.force.model.Device
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DeviceViewModel.Factory::class)
class DeviceViewModel @AssistedInject constructor(
    @Assisted val deviceAddress: String,
    private val factory: BluetoothClientDeviceConnection.Factory,
    private val savedDevicesRepository: SavedDevicesRepository,
) : ViewModel() {
    private val _remoteDevice = MutableStateFlow<RemoteDeviceImpl?>(null)
    val remoteDevice: StateFlow<RemoteDeviceImpl?> = _remoteDevice

    val protocol = MutableStateFlow(Device.Protocol.EPHEMERAL)

    val passPhrase = MutableStateFlow(PASS_PHRASE)
    val isPassPhraseSet = MutableStateFlow<Boolean?>(null)

    init {
        Log.d(TAG, "Creating ViewModel for device: $deviceAddress $this")
        viewModelScope.launch {
            val device = savedDevicesRepository.getDevice(deviceAddress)
            device?.passphrase?.let {
                passPhrase.value = it
                isPassPhraseSet.value = true
            } ?: run {
                isPassPhraseSet.value = false
            }
            val protocol = device?.protocol ?: Device.Protocol.EPHEMERAL
            _remoteDevice.value = RemoteDeviceImpl(
                scope = viewModelScope,
                connection = run {
                    val protocol = getProtocol(protocol)
                    factory.create(
                        deviceAddress = deviceAddress,
                        scope = viewModelScope,
                        protocol = protocol
                    )
                }
            )
            isPassPhraseSet.first { it == true }
            _remoteDevice.value?.start()
        }

        viewModelScope.launch {
            _remoteDevice.value?.state
                ?.filterIsInstance<RemoteDevice.State.Connected>()
                ?.collect {
                    savedDevicesRepository.addDevice(it.device.copy(passphrase = passPhrase.value))
                }
        }
    }

    override fun onCleared() {
        Log.d(TAG, "Clearing ViewModel for device: $deviceAddress")
        _remoteDevice.value?.close()
    }

    fun <T> onValueChanged(
        parameterId: Int,
        value: T
    ) {
        _remoteDevice.value?.setParameterValue(parameterId, value)
    }

    private fun getProtocol(protocol: Device.Protocol): Protocol {
        return when (protocol) {
            Device.Protocol.EPHEMERAL -> EcdhAesProtocol(
                serializer = ConfSerializer(),
                parser = ConfParser(),
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
                        return passPhrase.value
                    }
                },
                false,
                header = "guard\n".toByteArray(Charsets.UTF_8)
            )

            Device.Protocol.PASSPHRASE -> PassPhraseAesProtocol(
                serializer = ConfSerializer(),
                parser = ConfParser(),
                cryptoProducer = object : PassPhraseAesProtocol.CryptoProducer {
                    private lateinit var crypto: CryptoAes
                    override fun init() {
                        crypto = CryptoAes()
                    }

                    override fun getDecrypt(): (ByteArray) -> ByteArray = crypto::decryptData

                    override fun getEncrypt(): (ByteArray) -> ByteArray = crypto::encryptDataWhole
                },
                header = "guard\n".toByteArray(Charsets.UTF_8)
            )

            Device.Protocol.RAW -> RawProtocol(
                serializer = ConfSerializer(),
                parser = ConfParser(),
                bindPhraseProducer = object : RawProtocol.BindPhraseProducer {
                    override fun getBindPhrase(): String {
                        return passPhrase.value
                    }
                },
                false,
                header = "guard\n".toByteArray(Charsets.UTF_8)
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            deviceAddress: String
        ): DeviceViewModel
    }
}
