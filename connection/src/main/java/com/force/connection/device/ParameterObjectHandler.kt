package com.force.connection.device

interface ParameterObjectHandler {
    suspend fun handleInfo(obj: Any)
    suspend fun handleUpdate(obj: Any)
    fun <T> setParameterValue(id: Int, value: T)
    fun cancel()
}
