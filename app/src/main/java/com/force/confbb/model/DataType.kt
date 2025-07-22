package com.force.confbb.model

/*
        #define TYPE_HANDSHAKE_REQUEST          0x00
        #define TYPE_HANDSHAKE_RESPONSE         0x01
*/

sealed class DataType {
    data object HandshakeRequest : DataType()
    data object HandshakeResponse : DataType()
    data object ParameterInfo : DataType()

    companion object {
        fun fromCode(code: Byte): DataType {
            return when (code.toInt()) {
                0x00 -> HandshakeRequest
                0x01 -> HandshakeResponse
                0x02 -> ParameterInfo
                else -> throw IllegalArgumentException("Unknown DataType byte: $code")
            }
        }
    }
}
