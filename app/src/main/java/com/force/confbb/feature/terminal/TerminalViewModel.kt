package com.force.confbb.feature.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.force.confbb.data.BluetoothDeviceConnection
import com.force.confbb.model.DeviceConnectionStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = TerminalViewModel.Factory::class)
class TerminalViewModel @AssistedInject constructor(
    @Assisted val deviceAddress: String,
    factory: BluetoothDeviceConnection.Factory
) : ViewModel() {

    private val connectionRepository = factory.create(deviceAddress, viewModelScope)

    private val _items = MutableStateFlow<List<DeviceConnectionStatus>>(listOf(DeviceConnectionStatus.Disconnected))
    val items: StateFlow<List<DeviceConnectionStatus>> = _items

    init {
        viewModelScope.launch {
            connectionRepository.data.collect { status ->
                _items.value += status
                if (_items.value.size > 256) {
                    _items.value = _items.value.takeLast(128)
                }
            }
        }
    }

    fun send(text: String) {
        connectionRepository.send(("$text\n").toByteArray())
    }

    override fun onCleared() {
        super.onCleared()
        connectionRepository.close()
    }

    @AssistedFactory
    interface Factory {
        fun create(
            deviceAddress: String,
        ): TerminalViewModel
    }
}
