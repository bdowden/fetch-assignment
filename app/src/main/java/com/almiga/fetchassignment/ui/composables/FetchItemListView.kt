package com.almiga.fetchassignment.ui.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.almiga.fetchassignment.viewModels.ItemListViewState
import com.almiga.fetchassignment.viewModels.RetrieveStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FetchItemListView(
    viewState: ItemListViewState,
    refreshData: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = viewState.status

    if (status == RetrieveStatus.LOADING) {
        Loading(modifier)
    } else {
        PullToRefreshBox(
            onRefresh = refreshData,
            isRefreshing = status == RetrieveStatus.LOADING,
            modifier = modifier.then(
                Modifier
                    .padding(
                        top = 10.dp,
                    )
                    .fillMaxWidth()
            ),
        ) {
            when (status) {
                RetrieveStatus.SUCCESS -> Success(viewState)
                RetrieveStatus.ERROR -> Error(modifier)
                else -> {}
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun Success(viewState: ItemListViewState) {
    val groupedItems = remember { viewState.groupedItems }
    if (groupedItems.isEmpty()) {
        Text(
            text = "No Results Found",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
    } else {
        LazyColumn {
            groupedItems.forEach { (listId, elements) ->
                stickyHeader(key = listId) {
                    GroupHeader(listId)
                }
                items(elements) { item ->
                    FetchItemView(
                        modifier = Modifier.fillMaxWidth(),
                        item = item,
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(listId: String) {
    Text(
        text = listId,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        textAlign = TextAlign.Start,
        style = TextStyle(
            textIndent = TextIndent(
                firstLine = 10.sp,
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color.Gray)
            .wrapContentHeight()
    )
}

@Composable
private fun Loading(modifier: Modifier) {

    Text(
        text = "Loading...",
        fontSize = 20.sp,
        textAlign = TextAlign.Center,
        modifier = modifier.then(
            Modifier
                .fillMaxSize()
                .wrapContentHeight()
        )

    )
}

@Composable
private fun Error(modifier: Modifier) {
    /*Box(
        modifier = modifier.then(
            Modifier
                .fillMaxSize()
        )
    ) {*/
        Text(
            text = "An error has occurred. Please try again.",
            color = Color.Red,
            textAlign = TextAlign.Center,
            modifier = modifier.then(
                Modifier
                    .fillMaxSize()
                    .wrapContentHeight()
            )

        )
    //}
}