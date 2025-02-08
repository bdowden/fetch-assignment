package com.almiga.fetchassignment.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import com.almiga.fetchassignment.model.FetchItem

@Composable
fun FetchItemView(
    modifier: Modifier,
    item: FetchItem,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = item.name!!,
            fontStyle = FontStyle.Normal,
        )

        Text(
            text = item.id.toString(),
            fontStyle = FontStyle.Italic,
        )
    }
}