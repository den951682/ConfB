package com.force.confbb.serialization

import com.force.connection.protocol.PassPhraseAesProtocol
import com.force.model.toDataType
import com.google.protobuf.GeneratedMessageLite

class ConfSerializer : PassPhraseAesProtocol.Serializer {
    override fun serialize(data: Any): ByteArray {
        val code = data.toDataType().code
        val serializedData = (data as GeneratedMessageLite<*, *>).toByteArray()
        return byteArrayOf(code) + serializedData
    }
}
