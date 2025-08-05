package com.force.connection.protocol

interface ProtocolSerializer {
    fun serialize(data: Any): ByteArray
}
