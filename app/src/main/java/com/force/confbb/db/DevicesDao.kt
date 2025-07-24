package com.force.confbb.db

import androidx.room.*

@Dao
interface DevicesDao {
    @Query("SELECT * FROM devices")
    suspend fun getAll(): List<DeviceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: DeviceEntity)

    @Delete
    suspend fun delete(device: DeviceEntity)

    @Query("DELETE FROM devices WHERE address = :id")
    suspend fun deleteById(id: String)
}
