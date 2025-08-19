package com.force.confbb.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.force.misc.PASS_PHRASE
import com.force.model.Device

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val address: String, // MAC address
    val name: String?,
    val passphrase: String = PASS_PHRASE,
    val lastSeen: Long = 0,
    val protocol: Device.Protocol = Device.Protocol.EPHEMERAL
)

fun DeviceEntity.toDevice(): Device {
    return Device(
        name = this.name ?: address,
        address = this.address,
        passphrase = this.passphrase,
        lastSeen = this.lastSeen,
        protocol = this.protocol
    )
}

fun Device.toEntity(): DeviceEntity {
    return DeviceEntity(
        address = this.address,
        name = this.name,
        passphrase = this.passphrase,
        lastSeen = this.lastSeen,
        protocol = this.protocol
    )
}
