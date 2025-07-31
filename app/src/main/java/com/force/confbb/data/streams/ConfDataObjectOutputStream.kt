import com.force.confbb.data.streams.EncodeFrameOutputStream
import com.force.confbb.model.toDataType
import com.google.protobuf.GeneratedMessageLite
import java.io.FilterOutputStream
import java.io.OutputStream

class ConfDataObjectOutputStream(private val outputStream: OutputStream) : FilterOutputStream(outputStream) {
    fun writeDataObject(dataObject: Any) {
        val code = dataObject.toDataType().code
        val serializedData = (dataObject as GeneratedMessageLite<*, *>).toByteArray()
        if (outputStream is EncodeFrameOutputStream) {
            outputStream.writeData(byteArrayOf(code) + serializedData)
        } else {
            write(byteArrayOf(code) + serializedData)
        }
    }
}
