package com.force.confbb.data.streams

import android.util.Log
import com.force.confb.pmodel.BooleanParameter
import com.force.confb.pmodel.FloatParameter
import com.force.confb.pmodel.HandshakeResponse
import com.force.confb.pmodel.IntParameter
import com.force.confb.pmodel.Message
import com.force.confb.pmodel.ParameterInfo
import com.force.confb.pmodel.StringParameter
import com.force.confbb.model.ConfError
import com.force.confbb.model.ConfError.Companion.toCode
import com.force.confbb.model.DataType
import com.force.confbb.util.TAG
import java.io.OutputStream

class ConfDataObjectInputStream(
    private val decodeFrameInputStream: DecodeFrameInputStream,
    private val errorOutput: OutputStream
) {
    fun readDataObject(): Any {
        decodeFrameInputStream.readDecryptedFrame().let { frame ->
            val dataType = DataType.fromCode(frame[0])
            val dataToParse = frame.drop(1).toByteArray()
            return when (dataType) {
                is DataType.HandshakeResponse -> HandshakeResponse.parseFrom(dataToParse)
                is DataType.ParameterInfo -> ParameterInfo.parseFrom(dataToParse)
                is DataType.TypeInt -> IntParameter.parseFrom(dataToParse)
                is DataType.TypeFloat -> FloatParameter.parseFrom(dataToParse)
                is DataType.TypeString -> StringParameter.parseFrom(dataToParse)
                is DataType.TypeBoolean -> BooleanParameter.parseFrom(dataToParse)
                is DataType.TypeMessage -> Message.parseFrom(dataToParse)

                else -> {
                    Log.d(TAG, "Unhandled received data type: ${frame[0]}")
                    errorOutput.write(ConfError.NotSupportedError().toCode())
                }
            }
        }
    }
}
