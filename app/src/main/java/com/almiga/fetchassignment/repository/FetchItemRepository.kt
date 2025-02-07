package com.almiga.fetchassignment.repository

import com.almiga.fetchassignment.model.FetchItem
import com.almiga.fetchassignment.services.FetchService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ItemRepository {
    suspend fun retrieveItems(force: Boolean = false): List<FetchItem>
}

class FetchItemRepository @Inject constructor(
    private val fetchService: FetchService,
    private val dispatchProvider: Dispatchers,
) : ItemRepository {
    // These are here for a fake local storage - we could use Room here if we wanted
    private var lastResult: List<FetchItem>? = null
    private var lastFetchTime: Long = 0

    override suspend fun retrieveItems(force: Boolean): List<FetchItem> {
        return withContext(dispatchProvider.IO) {
            val didExpire = lastResult == null || lastFetchTime > 0
            if (force || didExpire) {
                fetchFromApi()
            } else {
                fetchFromCache()
            }
        }
    }

    private fun fetchFromCache(): List<FetchItem> {
        return lastResult ?: emptyList()
    }

    private suspend fun fetchFromApi(): List<FetchItem> {
        val result = fetchService.getItems()

        val items = if (result.isSuccessful) {
            result.body() ?: emptyList()
        } else {
            // Log the error
            emptyList()
        }

        return items.also {
            lastResult = it
            lastFetchTime = 1
        }
    }
}