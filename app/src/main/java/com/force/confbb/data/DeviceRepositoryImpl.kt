package com.force.confbb.data

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.RequiresPermission
import com.force.confbb.di.ApplicationScope
import com.force.confbb.util.BluetoothMonitor
import com.force.model.Device
import com.force.model.ScanDevicesStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothManager: BluetoothManager,
    private val bluetoothMonitor: BluetoothMonitor,
    private val savedDevicesRepository: SavedDevicesRepository,
    @ApplicationScope private val scope: CoroutineScope
) : DevicesRepository {
    private val receiver = object : BroadcastReceiver() {
        @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)?.let { device ->
                        val deviceHardwareAddress = device.address
                        val deviceName = device.name ?: deviceHardwareAddress ?: "Unknown"
                        Device(deviceName, deviceHardwareAddress, "", System.currentTimeMillis())
                            .also { newDevice ->
                                _devices.value.find { it.address == newDevice.address }?.let {
                                    _devices.value = _devices.value.filter { it.address != newDevice.address }
                                }
                            }
                    }?.let {
                        scope.launch {
                            savedDevicesRepository.setName(it.address, it.name)
                            savedDevicesRepository.setLastSeen(it.address, System.currentTimeMillis())
                        }
                        _devices.value += it
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _status.value = ScanDevicesStatus.SUCCESS
                    context.unregisterReceiver(this)
                }
            }
        }
    }

    override val enabled: Flow<Boolean> = bluetoothMonitor.isEnabled

    private val _status = MutableStateFlow(ScanDevicesStatus.IDLE)

    override val status: Flow<ScanDevicesStatus> = _status

    private val _devices = MutableStateFlow(emptyList<Device>())

    override val devices: Flow<List<Device>> = _devices

    private val filter = IntentFilter().apply {
        addAction(BluetoothDevice.ACTION_FOUND)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun startScan() {
        context.registerReceiver(receiver, filter)
        _devices.value = emptyList()
        runCatching {
            if (bluetoothManager.adapter.startDiscovery()) {
                _status.value = ScanDevicesStatus.SCANNING
            } else {
                _status.value = ScanDevicesStatus.FAILED
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun stopScan() {
        bluetoothManager.adapter.cancelDiscovery()
        kotlin.runCatching {
            context.unregisterReceiver(receiver)
        }
    }
}
