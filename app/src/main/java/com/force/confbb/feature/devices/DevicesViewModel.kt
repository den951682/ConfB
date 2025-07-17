package com.force.confbb.feature.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.force.confbb.data.DevicesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val devicesRepository: DevicesRepository
) : ViewModel() {
    val isBluetoothEnabled = devicesRepository.enabled.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )
}
