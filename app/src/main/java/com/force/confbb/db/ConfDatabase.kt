package com.force.confbb.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DeviceEntity::class], version = 1)
abstract class ConfDatabase : RoomDatabase() {
    abstract fun devicesDao(): DevicesDao
}
