package com.force.confbb.model

data class Device(
    val name: String,
    val address: String,
    val passphrase: String = "",
    val lastSeen: Long = 0L
)
