package com.force.connection.device

/*



package com.force.connection.device

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.force.confb.pmodel.BooleanParameter
import com.force.confb.pmodel.FloatParameter
import com.force.confb.pmodel.HandshakeResponse
import com.force.confb.pmodel.IntParameter
import com.force.confb.pmodel.Message
import com.force.confb.pmodel.ParameterInfo
import com.force.confb.pmodel.SetBooleanParameter
import com.force.confb.pmodel.SetFloatParameter
import com.force.confb.pmodel.SetIntParameter
import com.force.confb.pmodel.SetStringParameter
import com.force.confb.pmodel.StringParameter
import com.force.confbb.analytics.AnalyticsLogger
import com.force.confbb.data.CryptoManager
import com.force.confbb.data.SavedDevicesRepository
import com.force.confbb.data.device.CifferDataReaderWriter
import com.force.confbb.model.Device
import com.force.confbb.model.DeviceParameter
import com.force.misc.TAG
import com.google.protobuf.ByteString
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.nio.charset.Charset
import kotlin.reflect.KClass

class AbstractRemoteDevice @AssistedInject constructor(
    @Assisted("address") address: String,
    @Assisted("pass") val passPhrase: String,
    @Assisted val scope: CoroutineScope,
    factory: BluetoothDeviceConnection.Factory,
    savedDevicesRepository: SavedDevicesRepository
) : RemoteDevice {
    private lateinit var cryptoManager: CryptoManager
            ;
    private val cifferDataReaderWriter = CifferDataReaderWriter(
        { cryptoManager.decryptData(it) },
        { cryptoManager.encryptDataWhole(it) }
    )
    private val connection = factory.create(
        deviceAddress = address,
        scope = scope,
        cifferDataReaderWriter
    ).also { it.start() }

    private val _parameters = mutableStateMapOf<Int, DeviceParameter<*>>()

    private val _events = MutableSharedFlow<RemoteDevice.Event>(
        replay = 0,
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val name: String
        get() = connection.info.name
    override val address: String
        get() = connection.info.address

    override val state: StateFlow<RemoteDevice.State> = connection.state.map {
        when (it) {
            is DeviceConnection.State.Connecting -> RemoteDevice.State.Connecting
            is DeviceConnection.State.Disconnected -> RemoteDevice.State.Disconnected
            is DeviceConnection.State.Connected -> RemoteDevice.State.Connected
            is DeviceConnection.State.Error -> RemoteDevice.State.Error(it.error)
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = RemoteDevice.State.Disconnected
    )

    override val parameters: SnapshotStateMap<Int, DeviceParameter<*>> = _parameters

    override val events: SharedFlow<RemoteDevice.Event> = _events

    private val debounceJobs = mutableMapOf<Int, Job>()

    init {
        scope.launch(Dispatchers.Default) {
            cryptoManager = CryptoManager(passPhrase)
        }
        scope.launch(Dispatchers.IO) {
            connection.dataObjects
                .filterIsInstance<ParameterInfo>()
                .collect { parameterInfo ->
                    _parameters[parameterInfo.id] = _parameters[parameterInfo.id].let { ep ->
                        when (parameterInfo.type) {
                            0 -> DeviceParameter(
                                parameterInfo.id,
                                ep?.value ?: 0,
                                ep?.name ?: parameterInfo.name.toStringUtf8(),
                                ep?.description ?: parameterInfo.description.toStringUtf8(),
                                ep?.minValue ?: parameterInfo.minValue.toInt(),
                                ep?.maxValue ?: parameterInfo.maxValue.toInt(),
                                ep?.editable ?: parameterInfo.editable,
                                ep?.editable ?: false
                            )

                            1 -> DeviceParameter(
                                parameterInfo.id,
                                ep?.value ?: 0f,
                                ep?.name ?: parameterInfo.name.toStringUtf8(),
                                ep?.description ?: parameterInfo.description.toStringUtf8(),
                                ep?.minValue ?: parameterInfo.minValue,
                                ep?.maxValue ?: parameterInfo.maxValue,
                                ep?.editable ?: parameterInfo.editable,
                                ep?.editable ?: false
                            )

                            2 -> DeviceParameter(
                                parameterInfo.id,
                                ep?.value ?: "",
                                ep?.name ?: parameterInfo.name.toStringUtf8(),
                                ep?.description ?: parameterInfo.description.toStringUtf8(),
                                editable = ep?.editable ?: parameterInfo.editable,
                                changeRequestSend = ep?.editable ?: false
                            )

                            else -> DeviceParameter(
                                parameterInfo.id,
                                ep?.value ?: false,
                                ep?.name ?: parameterInfo.name.toStringUtf8(),
                                ep?.description ?: parameterInfo.description.toStringUtf8(),
                                editable = ep?.editable ?: parameterInfo.editable,
                                changeRequestSend = ep?.editable ?: false
                            )

                        }
                    }
                }
        }
        scope.launch(Dispatchers.IO) {
            connection.dataObjects.collect {
                (it as? HandshakeResponse)?.let {
                    savedDevicesRepository.addDevice(
                        Device(
                            connection.info.name,
                            connection.info.address,
                            passPhrase,
                            System.currentTimeMillis()
                        )
                    )
                    savedDevicesRepository.setLastSeen(connection.info.address, System.currentTimeMillis())
                    savedDevicesRepository.setName(connection.info.address, connection.info.name)
                }
                (it as? Message)?.let {
                    _events.emit(object : RemoteDevice.Event {
                        override val id: Int = it.id
                        override val obj: Any = it
                    })
                }
                converters[it::class]?.let { handler -> handler(it) }
            }
        }
    }

    override fun <T> setParameterValue(id: Int, value: T) {
        val newParameter = (_parameters[id] as? DeviceParameter<Any>)
            ?.copy(changeRequestSend = true, value = value as Any)
        _parameters[id] = newParameter as DeviceParameter<*>
        val request = when (value) {
            is Int -> SetIntParameter.newBuilder().setId(id).setValue(value).build()
            is Float -> SetFloatParameter.newBuilder().setId(id).setValue(value).build()
            is String -> SetStringParameter.newBuilder().setId(id)
                .setValue(ByteString.copyFromUtf8(value.trim()))
                .build()

            is Boolean -> SetBooleanParameter.newBuilder().setId(id).setValue(value).build()
            else -> throw IllegalArgumentException("Unsupported parameter type: ${value!!::class.java}")
        }
        AnalyticsLogger.logParameterChanged(id)
        Log.d(TAG, "Scheduled change for parameter $id: $value")

        debounceJobs[id]?.cancel()
        debounceJobs[id] = scope.launch {
            delay(if (value is String) 3000 else 1000)
            Log.d(TAG, "Value sent for parameter $id: $value")
            connection.sendDataObject(request)
            debounceJobs.remove(id)
        }
    }

    override fun close() {
        connection.close()
        _parameters.clear()
    }

    private val converters: Map<KClass<*>, suspend (Any) -> Unit> = mapOf(
        IntParameter::class to { param ->
            val p = param as IntParameter
            debounceJobs.remove(p.id)
            _parameters[p.id] = (_parameters[p.id] as? DeviceParameter<Int>)
                ?.copy(value = p.value, changeRequestSend = false)
                ?: DeviceParameter(p.id, value = p.value)
        },
        FloatParameter::class to { param ->
            val p = param as FloatParameter
            debounceJobs.remove(p.id)
            _parameters[p.id] = (_parameters[p.id] as? DeviceParameter<Float>)
                ?.copy(value = p.value, changeRequestSend = false)
                ?: DeviceParameter(p.id, value = p.value)
        },
        BooleanParameter::class to { param ->
            val p = param as BooleanParameter
            debounceJobs.remove(p.id)
            _parameters[p.id] = (_parameters[p.id] as? DeviceParameter<Boolean>)
                ?.copy(value = p.value, changeRequestSend = false)
                ?: DeviceParameter(p.id, value = p.value)
        },
        StringParameter::class to { param ->
            val p = param as StringParameter
            val value = p.value.toString(Charset.forName("UTF-8"))
            debounceJobs.remove(p.id)
            _parameters[p.id] = (_parameters[p.id] as? DeviceParameter<String>)
                ?.copy(value = value, changeRequestSend = false)
                ?: DeviceParameter(p.id, value = value)
        }
    )


    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("address") deviceAddress: String,
            @Assisted("pass") passPhrase: String,
            scope: CoroutineScope
        ): AbstractRemoteDevice
    }
}



private val cifferDataReaderWriter = CifferDataReaderWriter(
        { cryptoManager.decryptData(it) },
        { cryptoManager.encryptDataWhole(it) }
    )

 */



/*


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


 */

/*


val code = dataObject.toDataType().code
            val serializedData = (dataObject as GeneratedMessageLite<*, *>).toByteArray()
 */



/*


(it as? HandshakeResponse)?.let {
                    savedDevicesRepository.addDevice(
                        Device(
                            connection.info.name,
                            connection.info.address,
                            passPhrase,
                            System.currentTimeMillis()
                        )
                    )
                    savedDevicesRepository.setLastSeen(connection.info.address, System.currentTimeMillis())
                    savedDevicesRepository.setName(connection.info.address, connection.info.name)
                }


 */