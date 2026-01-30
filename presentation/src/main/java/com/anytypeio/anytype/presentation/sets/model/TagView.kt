package com.anytypeio.anytype.presentation.sets.model

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ui.ObjectIcon

data class TagView(val id: String, val tag: String, val color: String)

data class StatusView(val id: String, val status: String, val color: String)


sealed class ObjectView {

    abstract val id: Id
    abstract val icon: ObjectIcon
    abstract val name: String

    data class Default(
        override val id: String,
        override val name: String,
        override val icon: ObjectIcon,
        val types: List<String> = emptyList(),
        val isRelation: Boolean = false
    ) : ObjectView()

    data class Deleted(
        override val id: String,
        override val name: String,
        override val icon: ObjectIcon = ObjectIcon.Deleted,
    ) : ObjectView()
}

data class FileView(
    val id: String,
    val ext: String,
    val mime: String,
    val name: String,
    val icon: ObjectIcon
)