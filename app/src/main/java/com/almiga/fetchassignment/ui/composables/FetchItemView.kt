package com.almiga.fetchassignment.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.almiga.fetchassignment.model.FetchItem

@Composable
fun FetchItemView(
    modifier: Modifier,
    item: FetchItem,
) {
    Column(
        modifier = modifier.then(
            Modifier.padding(
                horizontal = 10.dp,
                vertical = 5.dp,
            )
        ),
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