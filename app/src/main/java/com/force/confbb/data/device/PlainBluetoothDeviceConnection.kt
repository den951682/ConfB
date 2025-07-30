package com.force.confbb.data.device

import android.bluetooth.BluetoothManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class PlainBluetoothDeviceConnection(
    deviceAddress: String,
    private val scope: CoroutineScope,
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
}
