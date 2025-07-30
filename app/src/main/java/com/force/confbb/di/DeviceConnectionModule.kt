package com.force.confbb.di

import com.force.confbb.data.device.AbstractDeviceConnection
import com.force.confbb.data.device.CifferDataReaderWriter
import com.force.confbb.data.device.PlainDataReaderWriter
import dagger.MapKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME


@Qualifier
@Retention(RUNTIME)
annotation class ReaderWriter(val readerWriter: ReaderWriters)

@MapKey
annotation class ReaderWriterKey(val value: ReaderWriters)

enum class ReaderWriters {
    PLAIN,
    CIFFER,
}

@Module
@InstallIn(SingletonComponent::class)
object ReaderWriterMapModule {
    @Provides
    @IntoMap
    @ReaderWriterKey(ReaderWriters.PLAIN)
    fun providePlain(): AbstractDeviceConnection.DataReaderWriter = PlainDataReaderWriter()

    @Provides
    @IntoMap
    @ReaderWriterKey(ReaderWriters.CIFFER)
    fun provideCiffer(): AbstractDeviceConnection.DataReaderWriter = CifferDataReaderWriter()
}
