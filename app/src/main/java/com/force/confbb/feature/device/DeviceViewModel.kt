package com.force.confbb.feature.device

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.force.confbb.data.BluetoothRemoteDevice
import com.force.confbb.util.TAG
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel(assistedFactory = DeviceViewModel.Factory::class)
class DeviceViewModel @AssistedInject constructor(
    @Assisted val deviceAddress: String,
    factory: BluetoothRemoteDevice.Factory
) : ViewModel() {

    val remoteDevice = factory.create(deviceAddress, viewModelScope)

    init {
        Log.d(TAG, "Creating ViewModel for device: $deviceAddress $this")
    }

    fun send(text: String) {

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
            deviceAddress: String,
        ): DeviceViewModel
    }
}
