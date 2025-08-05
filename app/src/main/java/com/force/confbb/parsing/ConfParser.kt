package com.force.confbb.parsing

import android.util.Log
import com.force.confb.pmodel.BooleanParameter
import com.force.confb.pmodel.FloatParameter
import com.force.confb.pmodel.HandshakeResponse
import com.force.confb.pmodel.IntParameter
import com.force.confb.pmodel.Message
import com.force.confb.pmodel.ParameterInfo
import com.force.confb.pmodel.StringParameter
import com.force.connection.protocol.ProtocolParser
import com.force.misc.TAG
import com.force.model.ConfException
import com.force.model.DataType

class ConfParser : ProtocolParser {
    override fun parse(data: ByteArray): Any {
        val dataTypeCode = data[0]
        val dataToParse = data.drop(1).toByteArray()
        val dataType = DataType.fromCode(dataTypeCode)
        return when (dataType) {
            is DataType.HandshakeResponse -> HandshakeResponse.parseFrom(dataToParse)
            is DataType.ParameterInfo -> ParameterInfo.parseFrom(dataToParse)
            is DataType.TypeInt -> IntParameter.parseFrom(dataToParse)
            is DataType.TypeFloat -> FloatParameter.parseFrom(dataToParse)
            is DataType.TypeString -> StringParameter.parseFrom(dataToParse)
            is DataType.TypeBoolean -> BooleanParameter.parseFrom(dataToParse)
            is DataType.TypeMessage -> Message.parseFrom(dataToParse)

            else -> {
                Log.d(TAG, "Unhandled received data type: $dataTypeCode")
                throw ConfException.NotSupportedException("Unhandled received data type: $dataTypeCode")
            }
        }
    }
}
