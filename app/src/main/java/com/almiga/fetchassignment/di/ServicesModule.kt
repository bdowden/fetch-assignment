package com.almiga.fetchassignment.di

import com.almiga.fetchassignment.services.FetchService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import retrofit2.Retrofit

@Module
@InstallIn(ActivityRetainedComponent::class)
object ServicesModule {
    @Provides
    fun providesFetchService(
        retrofit: Retrofit,
    ): FetchService {
        return retrofit
            .create(FetchService::class.java)
    }
}