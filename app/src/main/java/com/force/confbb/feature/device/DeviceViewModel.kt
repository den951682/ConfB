package com.force.confbb.feature.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.force.confbb.data.CipherBluetoothDeviceConnectionRepository
import com.force.confbb.model.DeviceConnectionStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel(assistedFactory = DeviceViewModel.Factory::class)
class DeviceViewModel @AssistedInject constructor(
    @Assisted val deviceAddress: String,
    factory: CipherBluetoothDeviceConnectionRepository.Factory
) : ViewModel() {

    private val connectionRepository = factory.create(deviceAddress)

    val message = connectionRepository.data.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DeviceConnectionStatus.Disconnected
    )

    fun send(text: String) {
        connectionRepository.send(text.toByteArray())
    }

    override fun onCleared() {
        super.onCleared()
        connectionRepository.close()
    }

    @AssistedFactory
    interface Factory {
        fun create(
            deviceAddress: String,
        ): DeviceViewModel
    }
}
