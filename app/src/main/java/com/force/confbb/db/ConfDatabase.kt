package com.force.confbb.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [DeviceEntity::class], version = 2)
@TypeConverters(ProtocolConverter::class)
abstract class ConfDatabase : RoomDatabase() {
    abstract fun devicesDao(): DevicesDao
}
