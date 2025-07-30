package com.force.confbb.data.device

import android.bluetooth.BluetoothManager
import com.force.confbb.data.BluetoothDeviceConnection
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader


class PlainBluetoothDeviceConnection @AssistedInject constructor(
    @Assisted deviceAddress: String,
    @Assisted private val scope: CoroutineScope,
    bluetoothManager: BluetoothManager,
) : AbstractBluetoothDeviceConnection(
    deviceAddress, scope, bluetoothManager
) {
    private lateinit var bufferedReader: BufferedReader

    override fun connect() {
        super.connect()
        bufferedReader = BufferedReader(InputStreamReader(input))
    }

    override fun readDataObject(): String = bufferedReader.readLine()

    override fun sendDataObject(dataObject: Any) {
        scope.launch(Dispatchers.IO) {
            send(dataObject.toString().toByteArray())
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(deviceAddress: String, scope: CoroutineScope): PlainBluetoothDeviceConnection
    }
}
