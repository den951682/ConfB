package com.force.test_connection_app

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.force.connection.connection.DeviceConnection
import com.force.connection.connection.impl.WifiClientDeviceConnection
import com.force.connection.connection.impl.WifiServerDeviceConnection
import com.force.connection.protocol.PlainProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val wifiServerFabric: WifiServerDeviceConnection.Factory,
    private val wifiClientFabric: WifiClientDeviceConnection.Factory
) : ViewModel() {
    private val _connection = MutableSharedFlow<DeviceConnection>()

    val connection: Flow<DeviceConnection> = _connection

    init {
        viewModelScope.launch {
            connection.flatMapLatest { it.state }
                .onEach { state ->
                    if (state is DeviceConnection.State.Error) {
                        Log.d(TAG, "Connection error: ${state.error.message}")
                    }
                }
                .collect()
        }
    }

    fun startWifiServer() {
        viewModelScope.launch {
            val c = wifiServerFabric.create(
                viewModelScope,
                PlainProtocol()
            )
            c.start()
            _connection.emit(c)
            var n = 0
            while (isActive) {
                c.sendDataObject(n++)
                delay(1000)
            }
        }
    }

    fun startWifiClient() {
        viewModelScope.launch {
            val c = wifiClientFabric.create(
                viewModelScope,
                PlainProtocol()
            )
            c.start()
            _connection.emit(c)
            while (isActive) {
                words.forEach {
                    c.sendDataObject(it)
                    delay(500)
                }
            }
        }
    }
}
