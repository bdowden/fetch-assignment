package com.almiga.fetchassignment.model

import androidx.compose.runtime.Stable

@Stable
data class FetchItem(
    val listId: Int,
    val id: Int,
    val name: String?,
) {
    val isValid: Boolean
        get() = !name.isNullOrBlank()
}
