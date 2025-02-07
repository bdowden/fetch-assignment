package com.almiga.fetchassignment.di

import com.almiga.fetchassignment.repository.FetchItemRepository
import com.almiga.fetchassignment.repository.ItemRepository
import com.almiga.fetchassignment.services.FetchService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.Dispatchers


@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {
    @Provides
    fun providesItemRepository(
        fetchService: FetchService
    ): ItemRepository {
        val dispatchProvider = Dispatchers

        return FetchItemRepository(
            fetchService = fetchService,
            dispatchProvider = dispatchProvider,
        )
    }
}