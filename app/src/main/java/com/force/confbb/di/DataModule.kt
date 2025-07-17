package com.force.confbb.di

import com.force.confbb.data.DevicesRepository
import com.force.confbb.data.FakeDevicesRepository
import com.force.confbb.data.FakeUserDataRepository
import com.force.confbb.data.UserDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindsUserDataRepository(
        userDataRepository: FakeUserDataRepository
    ): UserDataRepository

    @Binds
    abstract fun bindsDevicesRepository(
        devicesRepository: FakeDevicesRepository
    ): DevicesRepository
}
