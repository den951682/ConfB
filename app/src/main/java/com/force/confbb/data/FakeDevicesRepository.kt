package com.force.confbb.data

import com.force.confbb.di.ApplicationScope
import com.force.confbb.model.Device
import com.force.confbb.model.ScanDevicesStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

class FakeDevicesRepository @Inject constructor(
    @ApplicationScope
    private val scope: CoroutineScope
) : DevicesRepository {
    override val enabled = flowOf(false)
    private val _status = MutableStateFlow(ScanDevicesStatus.IDDLE)
    override val status = _status.asStateFlow()

    private val _devices: MutableStateFlow<List<Device>> = MutableStateFlow(emptyList())
    override val devices = _devices.asStateFlow()
    private val random = Random(System.nanoTime())

    override fun startScan() {
        scope.launch {
            _status.value = ScanDevicesStatus.SCANNING
            _devices.value = emptyList()
            delay(1000)
            _devices.value = listOf(
                Device("Device 1", "a1"),
            )
            delay(2500)
            if (random.nextBoolean()) {
                _devices.value = listOf(
                    Device("Device 1", "a1"),
                    Device("Device 2", "a2"), Device("Device 3", "a3"),
                    Device("Device 4", "a4"), Device("Device 5", "a5")
                )
                _status.value = ScanDevicesStatus.SUCCESS
            } else {
                _devices.value = emptyList()
                _status.value = ScanDevicesStatus.FAILED
            }
        }
    }

    override fun stopScan() {

    }
}
