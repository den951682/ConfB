package com.force.confbb.feature.device

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.force.connection.device.RemoteDeviceImpl
import com.force.connection.device.RemoteDevice
import com.force.confbb.data.SavedDevicesRepository
import com.force.misc.PASS_PHRASE
import com.force.misc.TAG
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DeviceViewModel.Factory::class)
class DeviceViewModel @AssistedInject constructor(
    @Assisted val deviceAddress: String,
    @Assisted val newDevice: Boolean,
    private val savedDevicesRepository: SavedDevicesRepository,
    factory: RemoteDeviceImpl.Factory,
) : ViewModel() {

    lateinit var remoteDevice: RemoteDevice

    val passPrase = MutableStateFlow(PASS_PHRASE)
    val isPassPhraseSet = MutableStateFlow<Boolean?>(null)

    init {
        Log.d(TAG, "Creating ViewModel for device: $deviceAddress $this")
        viewModelScope.launch {
            savedDevicesRepository.getDevice(deviceAddress)?.passphrase?.let {
                passPrase.value = it
                isPassPhraseSet.value = true
            } ?: run {
                isPassPhraseSet.value = false
            }
            isPassPhraseSet.collect {
                if (it == true) {
                    remoteDevice = factory.create(deviceAddress, passPrase.value.trim(), viewModelScope)
                }
            }
        }
    }

    override fun onCleared() {
        Log.d(TAG, "Clearing ViewModel for device: $deviceAddress")
        if (::remoteDevice.isInitialized) {
            remoteDevice.close()
        }
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
            deviceAddress: String,
            newDevice: Boolean
        ): DeviceViewModel
    }
}
