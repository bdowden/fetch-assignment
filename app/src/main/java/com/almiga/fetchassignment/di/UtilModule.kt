package com.almiga.fetchassignment.di

import com.almiga.fetchassignment.util.ClockSystemTimeProvider
import com.almiga.fetchassignment.util.SystemTimeProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UtilModule {
    @Binds
    abstract fun provideSystemTimeProvider(
        timeProvider: ClockSystemTimeProvider
    ): SystemTimeProvider
}