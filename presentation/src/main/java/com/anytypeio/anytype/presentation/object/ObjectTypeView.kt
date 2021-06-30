package com.anytypeio.anytype.presentation.`object`

sealed class ObjectTypeView {
    data class Item(
        val id: String,
        val name: String,
        val description: String?,
        val emoji: String?
    ) : ObjectTypeView()
}
