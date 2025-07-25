package com.force.confbb.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DevicesDao {

    @Query("SELECT * FROM devices")
    suspend fun getAll(): List<DeviceEntity>

    @Query("SELECT * FROM devices")
    fun observeAll(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE address = :id")
    suspend fun getDevice(id: String): DeviceEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(device: DeviceEntity)

    @Update
    suspend fun update(device: DeviceEntity)

    @Delete
    suspend fun delete(device: DeviceEntity)

    @Query("DELETE FROM devices WHERE address = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE devices SET lastSeen = :lastSeen WHERE address = :address")
    suspend fun setLastSeen(address: String, lastSeen: Long)

    @Query("UPDATE devices SET name = :name WHERE address = :address")
    suspend fun setName(address: String, name: String)
}
