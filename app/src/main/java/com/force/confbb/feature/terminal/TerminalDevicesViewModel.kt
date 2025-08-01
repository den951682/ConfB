package com.force.confbb.feature.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.force.confbb.data.DevicesRepository
import com.force.model.Device
import com.force.model.ScanDevicesStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TerminalDevicesViewModel @Inject constructor(
    private val devicesRepository: DevicesRepository,
) : ViewModel() {
    val isBluetoothEnabled = devicesRepository.enabled.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val status: StateFlow<ScanDevicesStatus> = devicesRepository.status.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScanDevicesStatus.IDLE,
    )

    val devices: StateFlow<List<Device>> = devicesRepository.devices.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    init {
        startScan()
    }

    fun startScan() {
        devicesRepository.startScan()
    }

    fun stopScan() {
        devicesRepository.stopScan()
    }
}
