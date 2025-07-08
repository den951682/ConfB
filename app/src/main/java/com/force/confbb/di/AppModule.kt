package com.force.confbb.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object AppModule {
    @Provides
    fun provideText(): String {
        return "Hello from AppModule!"
    }
}
