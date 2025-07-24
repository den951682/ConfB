package com.force.confbb.feature.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.force.confbb.data.DevicesRepository
import com.force.confbb.data.SavedDevicesRepository
import com.force.confbb.model.Device
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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

    val devices = savedDevicesRepository.devices

    fun onDeleteDevice(device: Device) {
        viewModelScope.launch {
            savedDevicesRepository.deleteDevice(device)
        }
    }

    fun onChangePassphrase(device: Device) {
        viewModelScope.launch {
            savedDevicesRepository.changePassphrase(device, "PiroJOKE")
        }
    }
}
