package com.force.connection.device

import com.force.confb.pmodel.BooleanParameter
import com.force.confb.pmodel.FloatParameter
import com.force.confb.pmodel.IntParameter
import com.force.confb.pmodel.ParameterInfo
import com.force.confb.pmodel.SetBooleanParameter
import com.force.confb.pmodel.SetFloatParameter
import com.force.confb.pmodel.SetIntParameter
import com.force.confb.pmodel.SetStringParameter
import com.force.confb.pmodel.StringParameter
import com.force.connection.CONN_TAG
import com.force.connection.ConnectionDefaults.log
import com.force.connection.ConnectionDefaults.logAnalytics
import com.force.model.DeviceParameter
import com.google.protobuf.ByteString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DefaultParameterObjectHandler(
    private val scope: CoroutineScope,
    private val send: (Any) -> Unit,
    private val parameters: MutableMap<Int, DeviceParameter<*>>,
) : ParameterObjectHandler {
    private val TEXT_DEBOUNCE = 5000L
    private val DEFAULT_DEBOUNCE = 1000L

    private val debounceJobs = mutableMapOf<Int, Job>()

    override suspend fun handleInfo(obj: Any) {
        if (obj is ParameterInfo) {
            handleParameterInfo(obj)
        } else {
            throw IllegalArgumentException("Expected ParameterInfo, got ${obj::class.java}")
        }
    }

    override suspend fun handleUpdate(obj: Any) {
        when (obj) {
            is IntParameter -> updateParameter(obj.id, obj.value)
            is FloatParameter -> updateParameter(obj.id, obj.value)
            is BooleanParameter -> updateParameter(obj.id, obj.value)
            is StringParameter -> updateParameter(
                obj.id,
                obj.value.toString(Charsets.UTF_8)
            )
        }
    }

    override fun <T> setParameterValue(id: Int, value: T) {
        val newParameter = (parameters[id] as? DeviceParameter<Any>)
            ?.copy(changeRequestSend = true, value = value as Any)
        parameters[id] = newParameter as DeviceParameter<*>
        val request = when (value) {
            is Int -> SetIntParameter.newBuilder().setId(id).setValue(value).build()
            is Float -> SetFloatParameter.newBuilder().setId(id).setValue(value).build()
            is String -> SetStringParameter.newBuilder().setId(id)
                .setValue(ByteString.copyFromUtf8(value.trim()))
                .build()

            is Boolean -> SetBooleanParameter.newBuilder().setId(id).setValue(value).build()
            else -> throw IllegalArgumentException("Unsupported parameter type: ${value!!::class.java}")
        }
        logAnalytics("parameter_changed", mapOf("id" to id.toString()))
        log(CONN_TAG, "Scheduled change for parameter $id: $value")

        debounceJobs[id]?.cancel()
        debounceJobs[id] = scope.launch {
            delay(if (value is String) TEXT_DEBOUNCE else DEFAULT_DEBOUNCE)
            log(CONN_TAG, "Value sent for parameter $id: $value")
            send(request)
            //connection.sendDataObject(request)
            debounceJobs.remove(id)
        }
    }

    private fun handleParameterInfo(parameterInfo: ParameterInfo) {
        parameters[parameterInfo.id] = parameters[parameterInfo.id].let { ep ->
            DeviceParameter(
                parameterInfo.id,
                ep?.value ?: 0f,
                ep?.name ?: parameterInfo.name.toStringUtf8(),
                ep?.description ?: parameterInfo.description.toStringUtf8(),
                ep?.minValue ?: parameterInfo.minValue,
                ep?.maxValue ?: parameterInfo.maxValue,
                ep?.editable ?: parameterInfo.editable,
                ep?.editable ?: false
            )
        }
    }

    private fun <T : Any> updateParameter(id: Int, value: T) {
        debounceJobs.remove(id)
        @Suppress("UNCHECKED_CAST")
        val param = parameters[id] as? DeviceParameter<T>
        parameters[id] = param?.copy(value = value, changeRequestSend = false)
            ?: DeviceParameter(id, value)
    }

    override fun cancel() {
        debounceJobs.forEach { it.value.cancel() }
        debounceJobs.clear()
    }
}
