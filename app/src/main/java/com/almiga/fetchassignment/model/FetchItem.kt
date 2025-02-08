package com.almiga.fetchassignment.model

data class FetchItem(
    val listId: Int,
    val id: Int,
    val name: String?,
) {
    val isValid: Boolean
        get() = !name.isNullOrBlank()
}
