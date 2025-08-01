package com.force.confbb.serialization

import com.force.connection.stream.ConfDataObjectOutputStream
import com.force.model.toDataType
import com.google.protobuf.GeneratedMessageLite

class ConfSerializer : ConfDataObjectOutputStream.Serializer {
    override fun serialize(dataObject: Any): ByteArray {
        val code = dataObject.toDataType().code
        val serializedData = (dataObject as GeneratedMessageLite<*, *>).toByteArray()
        return byteArrayOf(code) + serializedData
    }
}
