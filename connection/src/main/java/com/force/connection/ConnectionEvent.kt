package com.force.connection

sealed class ConnectionEvent {
    data class Error(val ex: Exception) : ConnectionEvent()
}
