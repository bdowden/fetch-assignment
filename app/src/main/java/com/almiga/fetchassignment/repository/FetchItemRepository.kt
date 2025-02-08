package com.almiga.fetchassignment.repository

import com.almiga.fetchassignment.model.FetchItem
import com.almiga.fetchassignment.services.FetchService
import com.almiga.fetchassignment.util.SystemTimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ItemRepository {
    suspend fun retrieveItems(force: Boolean = false): Result<List<FetchItem>>
}

class FetchItemRepository @Inject constructor(
    private val fetchService: FetchService,
    private val dispatchProvider: Dispatchers,
    private val systemTimeProvider: SystemTimeProvider,
) : ItemRepository {
    // These are here for a fake local storage - we could use Room here if we wanted
    private var lastResult: List<FetchItem> = emptyList()
    private var lastFetchTime: Long = 0

    override suspend fun retrieveItems(force: Boolean): Result<List<FetchItem>> {
        return withContext(dispatchProvider.IO) {

            val now = systemTimeProvider.getCurrentTimeMillis()

            val didExpire = now - lastFetchTime > CACHE_EXPIRATION

            try {
                val result = if (force || didExpire) {
                    fetchFromApi()
                } else {
                    fetchFromCache()
                }

                Result.Success(
                    result.filter { it.isValid }
                )

            } catch (ex: Exception) {
                Result.Failure(ex)
            }
        }
    }

    private fun fetchFromCache(): List<FetchItem> = lastResult

    private suspend fun fetchFromApi(): List<FetchItem> {
        val result = fetchService.getItems()

        return if (result.isSuccessful) {
            (result.body() ?: emptyList()).also {
                lastResult = it
                lastFetchTime = systemTimeProvider.getCurrentTimeMillis()
            }
        } else {
            val errorMessage = result.errorBody()?.toString() ?: ""
            throw FetchItemResultException(errorMessage)
        }
    }

    companion object {
        private const val CACHE_EXPIRATION = 20000L // millis
    }
}

class FetchItemResultException(message: String) : Exception(message) {}


// mockk was not working with `kotlin.Result` so I'm writing my own that will hopefully work better
sealed class Result<T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure<T>(val error: Throwable) : Result<T>()

    val isSuccess: Boolean
        get() = this is Success<T>

    val isFailure: Boolean
        get() = !isSuccess

    fun getOrNull(): T? {
        return (this as? Success<T>)?.let {
            value
        }
    }

    fun exceptionOrNull(): Throwable? {
        return (this as? Failure<T>)?.let {
            error
        }
    }

    companion object {
        fun<T> success(value: T): Success<T> = Success(value)
        fun<T> failure(error: Throwable): Failure<T> = Failure(error)
    }
}

inline fun <R, T> Result<T>.fold(
    onSuccess: (value: T) -> R,
    onFailure: (exception: Throwable) -> R
): R = when (this) {
    is Result.Success -> onSuccess(value)
    is Result.Failure -> onFailure(error)
}

