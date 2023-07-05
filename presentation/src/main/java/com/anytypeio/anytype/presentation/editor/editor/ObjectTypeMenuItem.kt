package com.anytypeio.anytype.presentation.editor.editor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType

sealed class ObjectTypeMenuItem {
    object ChangeType: ObjectTypeMenuItem()
    data class OpenSet(val set: Id, val typeName: String) : ObjectTypeMenuItem()
    data class CreateSet(val type: Id, val typeName: String) : ObjectTypeMenuItem()
}

fun ListenerType.Relation.ObjectType.items(set: Id? = null): List<ObjectTypeMenuItem> {
    return if (set.isNullOrBlank()) {
        listOf(
            ObjectTypeMenuItem.ChangeType,
            ObjectTypeMenuItem.CreateSet(
                type = typeId,
                typeName = typeName
            )
        )
    } else {
        listOf(
            ObjectTypeMenuItem.ChangeType,
            ObjectTypeMenuItem.OpenSet(
                set = set,
                typeName = typeName
            )
        )
    }
}

fun ListenerType.Relation.ObjectTypeDeleted.items(): List<ObjectTypeMenuItem> =
    listOf(ObjectTypeMenuItem.ChangeType)
