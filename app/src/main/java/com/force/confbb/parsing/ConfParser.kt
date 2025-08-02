package com.force.confbb.parsing

import android.util.Log
import com.force.confb.pmodel.BooleanParameter
import com.force.confb.pmodel.FloatParameter
import com.force.confb.pmodel.HandshakeResponse
import com.force.confb.pmodel.IntParameter
import com.force.confb.pmodel.Message
import com.force.confb.pmodel.ParameterInfo
import com.force.confb.pmodel.StringParameter
import com.force.connection.protocol.PassPhraseAesProtocol
import com.force.misc.TAG
import com.force.model.ConfException
import com.force.model.DataType

class ConfParser : PassPhraseAesProtocol.Parser {
    override fun parse(type: Byte, data: ByteArray): Any {
        val dataType = DataType.fromCode(type)
        return when (dataType) {
            is DataType.HandshakeResponse -> HandshakeResponse.parseFrom(data)
            is DataType.ParameterInfo -> ParameterInfo.parseFrom(data)
            is DataType.TypeInt -> IntParameter.parseFrom(data)
            is DataType.TypeFloat -> FloatParameter.parseFrom(data)
            is DataType.TypeString -> StringParameter.parseFrom(data)
            is DataType.TypeBoolean -> BooleanParameter.parseFrom(data)
            is DataType.TypeMessage -> Message.parseFrom(data)

            else -> {
                Log.d(TAG, "Unhandled received data type: $type")
                throw ConfException.NotSupportedException()
            }
        }
    }
}
