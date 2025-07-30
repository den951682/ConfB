package com.force.confbb.feature.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.force.confbb.data.device.PlainBluetoothDeviceConnection
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
    factory: PlainBluetoothDeviceConnection.Factory
) : ViewModel() {

    private val connection = factory.create(deviceAddress, viewModelScope)

    private val _items = MutableStateFlow<List<Pair<Boolean, String>>>(listOf())
    val items: StateFlow<List<Pair<Boolean, String>>> = _items
    val state = connection.state

    init {
        viewModelScope.launch {
            connection.dataObjects.collect { status ->
                _items.value += true to (status as String)
                if (_items.value.size > 256) {
                    _items.value = _items.value.takeLast(128)
                }
            }
        }
    }

    fun send(text: String) {
        connection.sendDataObject("$text\n")
        _items.value += false to text
    }

    override fun onCleared() {
        super.onCleared()
        connection.close()
    }

    @AssistedFactory
    interface Factory {
        fun create(
            deviceAddress: String,
        ): TerminalViewModel
    }
}
