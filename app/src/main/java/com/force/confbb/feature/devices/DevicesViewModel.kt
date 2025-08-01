package com.force.confbb.feature.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.force.confbb.data.DevicesRepository
import com.force.confbb.data.SavedDevicesRepository
import com.force.model.Device
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val devicesRepository: DevicesRepository,
    private val savedDevicesRepository: SavedDevicesRepository
) : ViewModel() {
    val isBluetoothEnabled = devicesRepository.enabled.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )
    private val tickerFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            devicesRepository.startScan()
            delay(10000)
            emit(System.currentTimeMillis())
            delay(60000)
        }
    }

    val devices = savedDevicesRepository.devices.combine(tickerFlow) { savedDevices, current ->
        SavedDeviceState.Loaded(savedDevices.map { it to (it.lastSeen > current - 70000) })
    }

    fun onDeleteDevice(device: Device) {
        viewModelScope.launch {
            savedDevicesRepository.deleteDevice(device.address)
        }
    }

    fun onChangePassphrase(device: Device, passPhrase: String) {
        viewModelScope.launch {
            savedDevicesRepository.changePassphrase(device, passPhrase)
        }
    }

    sealed class SavedDeviceState {
        data object Loading : SavedDeviceState()
        data class Loaded(
            val devices: List<Pair<Device, Boolean>>
        ) : SavedDeviceState()
    }
}
