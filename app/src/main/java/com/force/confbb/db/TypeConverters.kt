package com.force.confbb.db

import androidx.room.TypeConverter
import com.force.model.Device

class ProtocolConverter {
    @TypeConverter
    fun fromProtocol(value: Device.Protocol): String {
        return value.name
    }

    @TypeConverter
    fun toProtocol(value: String): Device.Protocol {
        return Device.Protocol.valueOf(value)
    }
}
