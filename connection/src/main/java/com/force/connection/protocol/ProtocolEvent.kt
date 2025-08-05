package com.force.connection.protocol

sealed class ProtocolEvent {
    class Header(val bytes: ByteArray) : ProtocolEvent()
    data class Error(val error: Throwable) : ProtocolEvent()
    data object Ready : ProtocolEvent()
}
