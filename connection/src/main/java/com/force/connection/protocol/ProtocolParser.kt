package com.force.connection.protocol

interface ProtocolParser {
    fun parse(data: ByteArray): Any
}
