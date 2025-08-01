package com.force.confbb.feature.device

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.force.confbb.data.CryptoManager
import com.force.confbb.data.SavedDevicesRepository
import com.force.confbb.parsing.ConfParser
import com.force.confbb.serialization.ConfSerializer
import com.force.connection.connection.BluetoothDeviceConnection
import com.force.connection.connection.DeviceConnection
import com.force.connection.device.CifferDataReaderWriter
import com.force.connection.device.RemoteDevice
import com.force.connection.device.RemoteDeviceImpl
import com.force.misc.PASS_PHRASE
import com.force.misc.TAG
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel(assistedFactory = DeviceViewModel.Factory::class)
class DeviceViewModel @AssistedInject constructor(
    @Assisted val deviceAddress: String,
    private val factory: BluetoothDeviceConnection.Factory,
    private val savedDevicesRepository: SavedDevicesRepository,
) : ViewModel() {

    val remoteDevice = RemoteDeviceImpl(
        scope = viewModelScope,
        connectionProvider = object : RemoteDeviceImpl.ConnectionProvider {
            override suspend fun getConnection(): DeviceConnection {
                val crypto = withContext(Dispatchers.Default) {
                    CryptoManager(passphrase = passPhrase.value.trim())
                }
                val dataReaderWriter = CifferDataReaderWriter(
                    serializer = ConfSerializer(),
                    parser = ConfParser(),
                    encrypt = crypto::encryptDataWhole,
                    decrypt = crypto::decryptData
                )
                return factory.create(
                    deviceAddress = deviceAddress,
                    scope = viewModelScope,
                    dataReaderWriter = dataReaderWriter
                )
            }
        }
    )

    val passPhrase = MutableStateFlow(PASS_PHRASE)
    val isPassPhraseSet = MutableStateFlow<Boolean?>(null)

    init {
        Log.d(TAG, "Creating ViewModel for device: $deviceAddress $this")
        viewModelScope.launch {
            savedDevicesRepository.getDevice(deviceAddress)?.passphrase?.let {
                passPhrase.value = it
                isPassPhraseSet.value = true
            } ?: run {
                isPassPhraseSet.value = false
            }
            isPassPhraseSet.collect {
                if (it == true) {
                    remoteDevice.start()
                }
            }
        }
        viewModelScope.launch {
            remoteDevice.state
                .filterIsInstance<RemoteDevice.State.Connected>()
                .collect {
                    savedDevicesRepository.addDevice(it.device.copy(passphrase= passPhrase.value))
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

    @AssistedFactory
    interface Factory {
        fun create(
            deviceAddress: String
        ): DeviceViewModel
    }
}
