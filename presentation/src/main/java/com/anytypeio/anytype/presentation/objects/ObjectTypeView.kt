package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.ObjectType

sealed class ObjectTypeView {

    abstract val id: String

    data class Item(
        override val id: String,
        val name: String,
        val description: String?,
        val emoji: String?,
        val layout: ObjectType.Layout
    ) : ObjectTypeView()

    data class Search(
        override val id: String = ""
    ) : ObjectTypeView()
}
