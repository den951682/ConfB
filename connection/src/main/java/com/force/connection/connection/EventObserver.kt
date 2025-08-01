package com.force.connection.connection

import com.force.connection.ConnectionEvent
import com.force.model.ConfException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.InputStream

class EventObserver(
    private val scope: CoroutineScope,
    private val input: InputStream,
    private val onError: suspend (ConnectionEvent) -> Unit
) {
    fun start() = scope.launch(Dispatchers.IO) {
        while (currentCoroutineContext().isActive) {
            val code = input.read()
            val error = ConnectionEvent.Error(ConfException.Companion.fromCode(code))
            onError(error)
        }
    }
}
