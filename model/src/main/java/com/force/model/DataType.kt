package com.force.model

import com.force.confb.pmodel.BooleanParameter
import com.force.confb.pmodel.FloatParameter
import com.force.confb.pmodel.HandshakeRequest
import com.force.confb.pmodel.HandshakeResponse
import com.force.confb.pmodel.IntParameter
import com.force.confb.pmodel.Message
import com.force.confb.pmodel.ParameterInfo
import com.force.confb.pmodel.SetBooleanParameter
import com.force.confb.pmodel.SetFloatParameter
import com.force.confb.pmodel.SetIntParameter
import com.force.confb.pmodel.SetStringParameter
import com.force.confb.pmodel.StringParameter

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
        #define TYPE_MESSAGE                    0x12
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
    data object TypeMessage : DataType(12)

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
                0x12 -> TypeMessage
                else -> throw IllegalArgumentException("Unknown DataType byte: $code")
            }
        }
    }
}

fun Any.toDataType(): DataType {
    return when (this) {
        is HandshakeRequest -> DataType.HandshakeRequest
        is HandshakeResponse -> DataType.HandshakeResponse
        is ParameterInfo -> DataType.ParameterInfo
        is DataType.Disconnect -> DataType.Disconnect
        is SetIntParameter -> DataType.SetInt
        is SetFloatParameter -> DataType.SetFloat
        is SetStringParameter -> DataType.SetString
        is SetBooleanParameter -> DataType.SetBoolean
        is IntParameter -> DataType.TypeInt
        is FloatParameter -> DataType.TypeFloat
        is StringParameter -> DataType.TypeString
        is BooleanParameter -> DataType.TypeBoolean
        is Message -> DataType.TypeMessage
        else -> throw IllegalArgumentException("This object not supported: ${this::class.simpleName}")
    }
}
