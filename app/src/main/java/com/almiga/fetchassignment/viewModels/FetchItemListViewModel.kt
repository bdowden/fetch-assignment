package com.almiga.fetchassignment.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.almiga.fetchassignment.model.FetchItem
import com.almiga.fetchassignment.repository.ItemRepository
import com.almiga.fetchassignment.repository.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FetchItemListViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
) : ViewModel() {
    private val _viewState = MutableStateFlow(ItemListViewState.EMPTY)
    val viewState = _viewState.asStateFlow()

    init {
        refreshItems(true)
    }

    private suspend fun retrieveItems(
        skipDelay: Boolean,
        forceRefresh: Boolean = false,
    ) {
        _viewState.update {
            it.copy(
                isRefreshing = true,
            )
        }

        if (!skipDelay) {
            delay(2000L) // Fake delay to show loading
        }
        val result = itemRepository.retrieveItems(
            force = forceRefresh,
        )

        result.fold(
            onSuccess = { items ->
                val sortedResult = items
                    .sortedWith(
                        compareBy<FetchItem> { it.listId }.thenBy { it.name }
                    )
                    .groupBy { it.listId.toString() }

                _viewState.update {
                    it.copy(
                        groupedItems = sortedResult,
                        isRefreshing = false,
                        isSuccess = true,
                    )
                }
            },
            onFailure = { error ->
                // TODO log the error somewhere better than console
                System.out.println(error)
                _viewState.update {
                    it.copy(
                        groupedItems = emptyMap(),
                        isRefreshing = false,
                        isSuccess = false,
                    )
                }
            }
        )
    }

    fun refreshItems() {
        refreshItems(false)
    }

    private fun refreshItems(skipDelay: Boolean) {
        viewModelScope.launch {
            retrieveItems(
                skipDelay = skipDelay,
                forceRefresh = true,
            )
        }

    }
}

data class ItemListViewState(
    val groupedItems: Map<String, List<FetchItem>>,
    val isSuccess: Boolean = false,
    val isRefreshing: Boolean = false,
) {

    companion object {
        val EMPTY = ItemListViewState(
            groupedItems = emptyMap(),
            isSuccess = false,
            isRefreshing = false,
        )
    }
}