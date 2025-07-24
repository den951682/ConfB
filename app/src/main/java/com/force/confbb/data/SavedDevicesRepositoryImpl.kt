package com.force.confbb.data

import com.force.confbb.db.DevicesDao
import com.force.confbb.db.toDevice
import com.force.confbb.db.toEntity
import com.force.confbb.model.Device
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedDevicesRepositoryImpl @Inject constructor(
    private val dao: DevicesDao
) : SavedDevicesRepository {

    override val devices: Flow<List<Device>> = dao.observeAll()
        .map { entities ->
            entities.map { it.toDevice() }
        }

    override suspend fun addDevice(device: Device) {
        dao.insert(device.toEntity())
    }

    override suspend fun changePassphrase(device: Device, newPassphrase: String) {
        val updated = device.copy(passphrase = newPassphrase)
        dao.update(updated.toEntity())
    }

    override suspend fun setLastSeen(id: String, lastSeen: Long) {
        dao.setLastSeen(id, lastSeen)
    }


    override suspend fun setName(id: String, name: String) {
        dao.setName(id, name)
    }

    override suspend fun deleteDevice(id: String) {
        dao.deleteById(id)
    }
}
