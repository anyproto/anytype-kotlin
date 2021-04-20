package com.anytypeio.anytype.presentation.sets.filter

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.relations.DateDescription

sealed class CreateFilterView {

    abstract val text: String
    abstract val isSelected: Boolean

    data class Tag(
        val id: Id,
        val name: String,
        val color: String,
        override val isSelected: Boolean
    ) : CreateFilterView() {
        override val text: String
            get() = name
    }

    data class Status(
        val id: Id,
        val name: String,
        val color: String,
        override val isSelected: Boolean
    ) : CreateFilterView() {
        override val text: String
            get() = name
    }

    data class Date(
        val id: Id,
        val description: String,
        val type: DateDescription,
        val timeInMillis: Long,
        override val isSelected: Boolean
    ) : CreateFilterView() {
        override val text: String
            get() = description
    }

    data class Object(
        val id: Id,
        val name: String,
        val image: String?,
        val emoji: String?,
        val type: String?,
        override val isSelected: Boolean
    ): CreateFilterView() {
        override val text: String
            get() = name
    }

    data class Checkbox(
        val isChecked: Boolean,
        override val isSelected: Boolean
    ): CreateFilterView() {
        override val text: String
            get() = ""
    }
}