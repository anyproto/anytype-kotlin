package com.anytypeio.anytype.presentation.navigation

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.presentation.objects.ObjectIcon

interface DefaultSearchItem

data class DefaultObjectView(
    val id: Id,
    val name: String,
    val type: String? = null,
    val typeName: String? = null,
    val layout: ObjectType.Layout? = null,
    val icon: ObjectIcon = ObjectIcon.None
) : DefaultSearchItem

data class ObjectView(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ObjectIcon,
    val layout: ObjectType.Layout? = null
)

fun ObjectView.isContainsText(text: String): Boolean = title.contains(text, true) ||
        subtitle.contains(text, true)

fun List<ObjectView>.filterBy(text: String): List<ObjectView> =
    if (text.isNotEmpty()) this.filter { it.isContainsText(text) } else this


sealed interface LibraryView {
    val id: Id
    val name: String

    class MyTypeView(
        override val id: Id,
        override val name: String,
        val icon: ObjectIcon? = null,
        val sourceObject: Id? = null,
        val readOnly: Boolean = false
    ) : LibraryView

    data class LibraryTypeView(
        override val id: Id,
        override val name: String,
        val icon: ObjectIcon? = null,
        val installed: Boolean = false,
    ) : LibraryView

    class MyRelationView(
        override val id: Id,
        override val name: String,
        val format: RelationFormat,
        val sourceObject: Id? = null,
        val readOnly: Boolean = false
    ) : LibraryView

    data class LibraryRelationView(
        override val id: Id,
        override val name: String,
        val format: RelationFormat,
        val installed: Boolean = false,
    ) : LibraryView

    class UnknownView(
        override val id: Id = "",
        override val name: String = "",
    ) : LibraryView

}