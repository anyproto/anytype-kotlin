package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.presentation.objects.ObjectIcon

sealed class RelationValueView {

    interface Selectable {
        // TODO make isSelected non-nullable
        val isSelected: Boolean?
    }

    object Empty : RelationValueView()

    data class Create(val name: String) : RelationValueView()

    sealed class Option : RelationValueView() {
        abstract val id: Id
        abstract val name: String

        data class Tag(
            override val id: Id,
            override val name: String,
            val color: String? = null,
            val removable: Boolean = false,
            val isCheckboxShown: Boolean,
            override val isSelected: Boolean
        ) : Option(), Selectable

        data class Status(
            override val id: Id,
            override val name: String,
            val removable: Boolean = false,
            val color: String? = null,
        ) : Option()
    }

    sealed class Object : RelationValueView(), Selectable {

        abstract val id: Id

        data class Default(
            override val id: Id,
            val name: String,
            val typeName: String?,
            val type: String?,
            val removeable: Boolean,
            val icon: ObjectIcon,
            val layout: ObjectType.Layout?,
            override val isSelected: Boolean? = null,
            val selectedNumber: String? = null
        ) : Object(), Selectable

        data class NonExistent(
            override val id: Id,
            override val isSelected: Boolean? = null,
            val removeable: Boolean
        ) : Object(), Selectable
    }

    data class File(
        val id: Id,
        val name: String,
        val mime: String,
        val ext: String,
        val removeable: Boolean = false,
        val image: Url?,
        override val isSelected: Boolean? = null,
        val selectedNumber: String? = null
    ) : RelationValueView(), Selectable
}