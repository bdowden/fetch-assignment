package com.almiga.fetchassignment.repository

import com.almiga.fetchassignment.model.FetchItem
import com.almiga.fetchassignment.services.FetchService
import com.almiga.fetchassignment.util.SystemTimeProvider
import io.mockk.clearAllMocks
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class FetchItemRepositoryTest {
    private lateinit var repository: FetchItemRepository
    private lateinit var fetchService: FetchService
    private lateinit var systemTimeProvider: SystemTimeProvider
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setup() {
        fetchService = mockk()
        systemTimeProvider = mockk()
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        repository = FetchItemRepository(
            fetchService = fetchService,
            dispatchProvider = Dispatchers,
            systemTimeProvider = systemTimeProvider
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `retrieveItems with force true always fetches from API`() = runTest {
        // Given
        val items = listOf(FetchItem(1, 1, "Item 1"))
        coEvery { fetchService.getItems() } returns Response.success(items)
        every { systemTimeProvider.getCurrentTimeMillis() } returns 1000L

        // When
        val result = repository.retrieveItems(force = true)

        // Then
        coVerify(exactly = 1) { fetchService.getItems() }
        assertTrue(result.isSuccess)
        assertEquals(items, result.getOrNull())
    }

    @Test
    fun `retrieveItems uses cache when not expired and force is false`() = runTest {
        // Given
        val items = listOf(FetchItem(1, 1, "Item 1"))
        coEvery { fetchService.getItems() } returns Response.success(items)
        every { systemTimeProvider.getCurrentTimeMillis() } returnsMany listOf(1000L, 2500L) // Within cache expiration

        // When
        repository.retrieveItems(force = true) // First call to populate cache
        
        // And
        clearMocks(fetchService)
        coEvery { fetchService.getItems() } returns Response.success(items)
        val result = repository.retrieveItems(force = false)

        // Then
        coVerify(exactly = 0) { fetchService.getItems() }
        assertTrue(result.isSuccess)
        assertEquals(items, result.getOrNull())
    }

    @Test
    fun `retrieveItems fetches from API when cache is expired`() = runTest {
        // Given
        val items = listOf(FetchItem(1, 1, "Item 1"))
        coEvery { fetchService.getItems() } returns Response.success(items)
        every { systemTimeProvider.getCurrentTimeMillis() } returnsMany listOf(1000L, 4000L) // Beyond cache expiration

        // When
        repository.retrieveItems(force = false) // First call to populate cache
        
        // And
        clearMocks(fetchService)
        coEvery { fetchService.getItems() } returns Response.success(items)
        val result = repository.retrieveItems(force = false)

        // Then
        coVerify(exactly = 1) { fetchService.getItems() }
        assertTrue(result.isSuccess)
        assertEquals(items, result.getOrNull())
    }

    @Test
    fun `retrieveItems filters out invalid items`() = runTest {
        // Given
        val items = listOf(
            FetchItem(1, 1, "Valid Item"),
            FetchItem(1, 2, null),
            FetchItem(1, 3, ""),
            FetchItem(1, 4, "  "),
            FetchItem(1, 5, "Another Valid Item")
        )
        coEvery { fetchService.getItems() } returns Response.success(items)
        every { systemTimeProvider.getCurrentTimeMillis() } returns 1000L

        // When
        val result = repository.retrieveItems(force = true)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertTrue(result.getOrNull()?.all { it.isValid } == true)
    }

    @Test
    fun `retrieveItems handles empty response from API`() = runTest {
        // Given
        coEvery { fetchService.getItems() } returns Response.success(emptyList())
        every { systemTimeProvider.getCurrentTimeMillis() } returns 1000L

        // When
        val result = repository.retrieveItems(force = true)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun `retrieveItems handles null body from API`() = runTest {
        // Given
        coEvery { fetchService.getItems() } returns Response.success(null)
        every { systemTimeProvider.getCurrentTimeMillis() } returns 1000L

        // When
        val result = repository.retrieveItems(force = true)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun `retrieveItems handles API error`() = runTest {
        // Given
        coEvery { fetchService.getItems() } returns Response.error(
            500,
            ResponseBody.create(null,
                "Internal Server Error",
            )
        )
        every { systemTimeProvider.getCurrentTimeMillis() } returns 1000L

        // When
        val result = repository.retrieveItems(force = true)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is FetchItemResultException)
    }

    @Test
    fun `retrieveItems handles network exception`() = runTest {
        // Given
        coEvery { fetchService.getItems() } throws Exception("Network error")
        every { systemTimeProvider.getCurrentTimeMillis() } returns 1000L

        // When
        val result = repository.retrieveItems(force = true)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `retrieveItems updates cache timestamp on successful API fetch`() = runTest {
        // Given
        val items = listOf(FetchItem(1, 1, "Item 1"))
        coEvery { fetchService.getItems() } returns Response.success(items)
        every { systemTimeProvider.getCurrentTimeMillis() } returnsMany listOf(1000L, 1500L)

        // When
        repository.retrieveItems(force = true)
        
        // Then
        coVerify(exactly = 2) { systemTimeProvider.getCurrentTimeMillis() }
    }
}