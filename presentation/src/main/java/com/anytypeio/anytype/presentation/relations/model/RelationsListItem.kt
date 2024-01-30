package com.anytypeio.anytype.presentation.relations.model

import com.anytypeio.anytype.core_models.ThemeColor

sealed class RelationsListItem {

    abstract val text: String

    sealed class Item : RelationsListItem() {
        data class Tag(
            override val text: String,
            val color: ThemeColor,
            val isSelected: Boolean,
            val number: Int? = null
        ) : Item()

        data class Status(
            override val text: String,
            val color: ThemeColor,
            val isSelected: Boolean
        ) : Item()
    }

    sealed class CreateItem(
        override val text: String
    ) : RelationsListItem() {
        class Tag(text: String) : CreateItem(text)
        class Status(text: String) : CreateItem(text)
    }
}