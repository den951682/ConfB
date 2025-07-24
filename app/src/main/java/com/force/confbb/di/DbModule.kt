package com.force.confbb.di

import android.content.Context
import androidx.room.Room
import com.force.confbb.db.ConfDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ConfDatabase {
        return Room.databaseBuilder(context, ConfDatabase::class.java, "db")
            .build()
    }
}
