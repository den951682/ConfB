package com.force.confbb.di

import com.force.confbb.data.DeviceRepositoryImpl
import com.force.confbb.data.DevicesRepository
import com.force.confbb.data.FakeSavedDeviceRepository
import com.force.confbb.data.FakeUserDataRepository
import com.force.confbb.data.SavedDevicesRepository
import com.force.confbb.data.UserDataRepository
import com.force.confbb.util.BluetoothMonitor
import com.force.confbb.util.BluetoothMonitorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindsUserDataRepository(
        userDataRepository: FakeUserDataRepository
    ): UserDataRepository

    @Binds
    @Singleton
    abstract fun bindsBluetoothMonitor(
        bluetoothMonitor: BluetoothMonitorImpl
    ): BluetoothMonitor

    @Binds
    @Singleton
    abstract fun bindsDevicesRepository(
        devicesRepository: DeviceRepositoryImpl
    ): DevicesRepository

    @Binds
    @Singleton
    abstract fun bindsSavedDevicesRepository(
        savedDevicesRepository: FakeSavedDeviceRepository
    ): SavedDevicesRepository
}
