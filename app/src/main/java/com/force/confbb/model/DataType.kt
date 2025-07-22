package com.force.confbb.model

import com.force.confb.pmodel.Disconnect

/*
        #define TYPE_HANDSHAKE_REQUEST          0x00
        #define TYPE_HANDSHAKE_RESPONSE         0x01
*/

sealed class DataType(val code: Byte) {
    data object HandshakeRequest : DataType(0)
    data object HandshakeResponse : DataType(1)
    data object ParameterInfo : DataType(2)
    data object Disconnect : DataType(3)

    companion object {
        fun fromCode(code: Byte): DataType {
            return when (code.toInt()) {
                0x00 -> HandshakeRequest
                0x01 -> HandshakeResponse
                0x02 -> ParameterInfo
                0x03 -> Disconnect
                else -> throw IllegalArgumentException("Unknown DataType byte: $code")
            }
        }
    }
}
