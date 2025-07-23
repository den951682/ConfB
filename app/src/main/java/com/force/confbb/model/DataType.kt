package com.force.confbb.model

import com.force.confb.pmodel.Disconnect

/*
        #define TYPE_HANDSHAKE_REQUEST          0x00
        #define TYPE_HANDSHAKE_RESPONSE         0x01
        #define TYPE_PARAMETER_INFO             0x02
        #define TYPE_DISCONNECT                 0x03
        #define TYPE_SET_INT                    0x04
        #define TYPE_SET_FLOAT                  0x05
        #define TYPE_SET_STRING                 0x06
        #define TYPE_SET_BOOLEAN                0x07
        #define TYPE_INT                        0x08
        #define TYPE_FLOAT                      0x09
        #define TYPE_STRING                     0x10
        #define TYPE_BOOLEAN                    0x11
*/

sealed class DataType(val code: Byte) {
    data object HandshakeRequest : DataType(0)
    data object HandshakeResponse : DataType(1)
    data object ParameterInfo : DataType(2)
    data object Disconnect : DataType(3)
    data object SetInt : DataType(4)
    data object SetFloat : DataType(5)
    data object SetString : DataType(6)
    data object SetBoolean : DataType(7)
    data object TypeInt : DataType(8)
    data object TypeFloat : DataType(9)
    data object TypeString : DataType(10)
    data object TypeBoolean : DataType(11)

    companion object {
        fun fromCode(code: Byte): DataType {
            return when (code.toInt()) {
                0x00 -> HandshakeRequest
                0x01 -> HandshakeResponse
                0x02 -> ParameterInfo
                0x03 -> Disconnect
                0x04 -> SetInt
                0x05 -> SetFloat
                0x06 -> SetString
                0x07 -> SetBoolean
                0x08 -> TypeInt
                0x09 -> TypeFloat
                0x10 -> TypeString
                0x11 -> TypeBoolean
                else -> throw IllegalArgumentException("Unknown DataType byte: $code")
            }
        }
    }
}
