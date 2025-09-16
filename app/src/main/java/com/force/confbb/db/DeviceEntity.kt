package com.force.confbb.db

import androidx.room.Entity
import com.force.misc.PASS_PHRASE
import com.force.model.Device

@Entity(
    tableName = "devices",
    primaryKeys = ["address", "loraAddress"]
)
data class DeviceEntity(
    val address: String, // MAC address
    val name: String?,
    val passphrase: String = PASS_PHRASE,
    val loraAddress: Int = 0,
    val lastSeen: Long = 0,
    val protocol: Device.Protocol = Device.Protocol.EPHEMERAL
)

fun DeviceEntity.toDevice(): Device {
    return Device(
        name = this.name ?: address,
        address = this.address,
        passphrase = this.passphrase,
        loraAddress = this.loraAddress,
        lastSeen = this.lastSeen,
        protocol = this.protocol
    )
}

fun Device.toEntity(): DeviceEntity {
    return DeviceEntity(
        address = this.address,
        name = this.name,
        passphrase = this.passphrase,
        loraAddress = this.loraAddress,
        lastSeen = this.lastSeen,
        protocol = this.protocol
    )
}
