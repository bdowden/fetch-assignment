package com.almiga.fetchassignment.repository

import com.almiga.fetchassignment.model.FetchItem
import com.almiga.fetchassignment.services.FetchService
import com.almiga.fetchassignment.util.SystemTimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ItemRepository {
    suspend fun retrieveItems(force: Boolean = false): List<FetchItem>
}

class FetchItemRepository @Inject constructor(
    private val fetchService: FetchService,
    private val dispatchProvider: Dispatchers,
    private val systemTimeProvider: SystemTimeProvider,
) : ItemRepository {
    // These are here for a fake local storage - we could use Room here if we wanted
    private var lastResult: List<FetchItem> = emptyList()
    private var lastFetchTime: Long = 0

    override suspend fun retrieveItems(force: Boolean): List<FetchItem> {
        return withContext(dispatchProvider.IO) {
            val now = systemTimeProvider.getCurrentTimeMillis()

            val didExpire = now - lastFetchTime > CACHE_EXPIRATION
            if (force || didExpire) {
                fetchFromApi()
            } else {
                fetchFromCache()
            }
        }.filterNot { it.name.isNullOrBlank() }
    }

    private fun fetchFromCache(): List<FetchItem> {
        return lastResult ?: emptyList()
    }

    private suspend fun fetchFromApi(): List<FetchItem> {
        val result = fetchService.getItems()

        return if (result.isSuccessful) {
            (result.body() ?: emptyList()).also {
                lastResult = it
                lastFetchTime = systemTimeProvider.getCurrentTimeMillis()
            }
        } else {
            // TODO Log the error
            emptyList()
        }
    }

    companion object {
        private const val CACHE_EXPIRATION = 2000L // millis
    }
}