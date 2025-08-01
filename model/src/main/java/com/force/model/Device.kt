package com.force.model

import com.force.misc.PASS_PHRASE

data class Device(
    val name: String,
    val address: String,
    val passphrase: String = PASS_PHRASE,
    val lastSeen: Long = 0L
)
