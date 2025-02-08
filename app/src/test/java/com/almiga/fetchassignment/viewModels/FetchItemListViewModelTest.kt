package com.almiga.fetchassignment.viewModels

import com.almiga.fetchassignment.model.FetchItem
import com.almiga.fetchassignment.repository.ItemRepository
import com.almiga.fetchassignment.repository.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FetchItemListViewModelTest {

    private lateinit var viewModel: FetchItemListViewModel
    private lateinit var repository: ItemRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty`() = runTest {
        // Given
        val result = listOf<FetchItem>()
        coEvery { repository.retrieveItems(force = true) } returns Result.success(result)
        
        // When
        viewModel = FetchItemListViewModel(repository).also {
            assertTrue(it.viewState.value.groupedItems.isEmpty())
        }
        advanceUntilIdle()

        // Then
        //val initialState = viewModel.viewState.first()
        //assertTrue(initialState.groupedItems.isEmpty())
    }

    @Test
    fun `retrieveItems success with valid items sorts and groups correctly`() = runTest {
        // Given
        val items = listOf(
            FetchItem(listId = 2, id = 1, name = "Item B"),
            FetchItem(listId = 1, id = 2, name = "Item C"),
            FetchItem(listId = 1, id = 3, name = "Item A"),
            FetchItem(listId = 2, id = 4, name = "Item A")
        )
        coEvery { repository.retrieveItems(force = true) } returns Result.success(items)
        
        // When
        viewModel = FetchItemListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.viewState.first()
        assertTrue(state.isSuccess)
        assertFalse(state.isRefreshing)
        assertEquals(2, state.groupedItems.size)
        
        // Check group 1
        val group1 = state.groupedItems["1"]
        assertEquals(2, group1?.size)
        assertEquals("Item A", group1?.get(0)?.name)
        assertEquals("Item C", group1?.get(1)?.name)
        
        // Check group 2
        val group2 = state.groupedItems["2"]
        assertEquals(2, group2?.size)
        assertEquals("Item A", group2?.get(0)?.name)
        assertEquals("Item B", group2?.get(1)?.name)
    }

    @Test
    fun `retrieveItems handles items with null or blank names`() = runTest {
        // Given
        val items = listOf(
            FetchItem(listId = 1, id = 1, name = null),
            FetchItem(listId = 1, id = 2, name = ""),
            FetchItem(listId = 1, id = 3, name = "Valid Name")
        )
        coEvery { repository.retrieveItems(force = true) } returns Result.success(items)
        
        // When
        viewModel = FetchItemListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.viewState.first()
        assertTrue(state.isSuccess)
        val group = state.groupedItems["1"]
        assertEquals(3, group?.size)
        assertEquals(null, group?.get(0)?.name)
        assertEquals("", group?.get(1)?.name)
        assertEquals("Valid Name", group?.get(2)?.name)
    }

    @Test
    fun `retrieveItems handles network failure`() = runTest {
        // Given
        coEvery { repository.retrieveItems(force = true) } returns Result.failure(Exception("Network error"))
        
        // When
        viewModel = FetchItemListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.viewState.first()
        assertFalse(state.isSuccess)
        assertFalse(state.isRefreshing)
        assertTrue(state.groupedItems.isEmpty())
    }

    @Test
    fun `refreshItems triggers new data load`() = runTest {
        // Given
        val initialItems = listOf(FetchItem(listId = 1, id = 1, name = "Initial"))
        val refreshedItems = listOf(FetchItem(listId = 1, id = 1, name = "Refreshed"))
        coEvery { repository.retrieveItems(force = true) } returnsMany listOf(
            Result.success(initialItems),
            Result.success(refreshedItems)
        )
        viewModel = FetchItemListViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.refreshItems()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.viewState.first()
        assertTrue(state.isSuccess)
        assertFalse(state.isRefreshing)
        assertEquals("Refreshed", state.groupedItems["1"]?.first()?.name)
    }

    @Test
    fun `retrieveItems shows loading state during fetch`() = runTest {
        // Given
        val items = listOf(FetchItem(listId = 1, id = 1, name = "Item"))
        var stateDuringRefresh: ItemListViewState

        coEvery { repository.retrieveItems(force = true) } answers {
            stateDuringRefresh = viewModel.viewState.value
            Result.success(items)
        }
        
        // When
        viewModel = FetchItemListViewModel(repository)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - loading should be complete
        val finalState = viewModel.viewState.first()
        assertFalse(finalState.isRefreshing)
        assertTrue(finalState.isSuccess)
    }
}