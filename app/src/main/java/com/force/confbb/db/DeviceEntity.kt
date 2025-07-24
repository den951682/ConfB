package com.force.confbb.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val address: String, // MAC address
    val name: String?,
    val isBonded: Boolean = false,
    val isAvailable: Boolean = false
)
