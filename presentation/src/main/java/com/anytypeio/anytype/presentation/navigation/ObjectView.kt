package com.anytypeio.anytype.presentation.navigation

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.presentation.objects.ObjectIcon

interface DefaultSearchItem

data class DefaultObjectView(
    val id: Id,
    val name: String,
    val type: String? = null,
    val typeName: String? = null,
    val description: String? = null,
    val layout: ObjectType.Layout? = null,
    val icon: ObjectIcon = ObjectIcon.None,
    val lastModifiedDate: Long = 0L,
    val lastOpenedDate: Long = 0L,
    val isFavorite: Boolean = false
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
