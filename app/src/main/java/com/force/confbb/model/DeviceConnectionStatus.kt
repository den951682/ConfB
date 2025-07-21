package com.force.confbb.model

sealed class DeviceConnectionStatus {
    data class Connected(val name: String) : DeviceConnectionStatus()
    data class Error(val trouble: Throwable? = null) : DeviceConnectionStatus()
    data object Disconnected : DeviceConnectionStatus()
    class Message(val byteArray: ByteArray) : DeviceConnectionStatus()
    class SendMessage(val byteArray: ByteArray) : DeviceConnectionStatus()
    class DataMessage(data: Any) : DeviceConnectionStatus()
}
