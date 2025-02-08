package com.almiga.fetchassignment.ui.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.almiga.fetchassignment.viewModels.ItemListViewState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FetchItemListView(
    viewState: ItemListViewState,
    refreshData: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = viewState.isRefreshing,
        onRefresh = refreshData,
        modifier = modifier
    ) {
        if (viewState.isSuccess) {
            LazyColumn {
                viewState.groupedItems.forEach { (listId, elements) ->
                    stickyHeader(key = listId) {
                        Text(
                            text = listId,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                    items(elements) { item ->
                        FetchItemView(
                            modifier = Modifier,
                            item = item,
                        )
                    }
                }
            }

        } else {
            Error(modifier)
        }
    }
}

@Composable
private fun Error(modifier: Modifier) {
    Box(
        modifier = modifier.then(
            Modifier
                .fillMaxSize()
        )
    ) {
        Text(
            text = "An error has occurred. Please try again.",
            color = Color.Red,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}