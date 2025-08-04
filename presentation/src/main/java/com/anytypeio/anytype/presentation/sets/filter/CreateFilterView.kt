package com.anytypeio.anytype.presentation.sets.filter

import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVFilterQuickOption
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.RelativeDate
import com.anytypeio.anytype.presentation.objects.ObjectIcon

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
        val type: DVFilterQuickOption,
        val condition: DVFilterCondition,
        val value: Long,
        override val isSelected: Boolean,
        val relativeDate: RelativeDate?
    ) : CreateFilterView() {
        override val text: String
            get() = description

        companion object {
            const val NO_VALUE = 0L
        }
    }

    data class Object(
        val id: Id,
        val name: String,
        val typeName: String?,
        val icon: ObjectIcon,
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