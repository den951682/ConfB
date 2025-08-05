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
import com.force.connection.protocol.PassPhraseAesProtocol
import com.force.crypto.CryptoManager
import com.force.misc.PASS_PHRASE
import com.force.misc.TAG
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DeviceViewModel.Factory::class)
class DeviceViewModel @AssistedInject constructor(
    @Assisted val deviceAddress: String,
    private val factory: BluetoothClientDeviceConnection.Factory,
    private val savedDevicesRepository: SavedDevicesRepository,
) : ViewModel() {

    val remoteDevice = RemoteDeviceImpl(
        scope = viewModelScope,
        connection = run {
            val protocol = PassPhraseAesProtocol(
                serializer = ConfSerializer(),
                parser = ConfParser(),
                cryptoProducer = object : PassPhraseAesProtocol.CryptoProducer {
                    private lateinit var crypto: CryptoManager
                    override fun init() {
                        crypto = CryptoManager(passphrase = passPhrase.value.trim())
                    }

                    override fun getDecrypt(): (ByteArray) -> ByteArray = crypto::decryptData

                    override fun getEncrypt(): (ByteArray) -> ByteArray = crypto::encryptDataWhole
                },
                header = "guard\n".toByteArray(Charsets.UTF_8)
            )
            factory.create(
                deviceAddress = deviceAddress,
                scope = viewModelScope,
                protocol = protocol
            )
        }
    )

    val passPhrase = MutableStateFlow(PASS_PHRASE)
    val isPassPhraseSet = MutableStateFlow<Boolean>(false)

    init {
        Log.d(TAG, "Creating ViewModel for device: $deviceAddress $this")
        viewModelScope.launch {
            getExistedPassphrase()
            isPassPhraseSet.first { it }
            remoteDevice.start()
        }
        viewModelScope.launch {
            remoteDevice.state
                .filterIsInstance<RemoteDevice.State.Connected>()
                .collect {
                    savedDevicesRepository.addDevice(it.device.copy(passphrase = passPhrase.value))
                }
        }
    }

    override fun onCleared() {
        Log.d(TAG, "Clearing ViewModel for device: $deviceAddress")
        remoteDevice.close()
    }

    fun <T> onValueChanged(
        parameterId: Int,
        value: T
    ) {
        remoteDevice.setParameterValue(parameterId, value)
    }

    private suspend fun getExistedPassphrase() {
        savedDevicesRepository.getDevice(deviceAddress)?.passphrase?.let {
            passPhrase.value = it
            isPassPhraseSet.value = true
        } ?: run {
            isPassPhraseSet.value = false
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            deviceAddress: String
        ): DeviceViewModel
    }
}
