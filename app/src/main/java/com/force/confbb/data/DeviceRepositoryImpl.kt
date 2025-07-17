package com.force.confbb.data

import com.force.confbb.model.Device
import com.force.confbb.model.ScanDevicesStatus
import com.force.confbb.util.BluetoothMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class DeviceRepositoryImpl @Inject constructor(
    private val bluetoothMonitor: BluetoothMonitor
) : DevicesRepository {
    override val enabled: Flow<Boolean> = bluetoothMonitor.isEnabled

    override val status: Flow<ScanDevicesStatus>
        get() = flowOf(ScanDevicesStatus.IDDLE)
    override val devices: Flow<List<Device>>
        get() = flowOf(emptyList())

    override fun startScan() {}

    override fun stopScan() {
    }
}
