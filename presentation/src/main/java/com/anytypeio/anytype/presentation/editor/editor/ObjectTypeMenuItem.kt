package com.anytypeio.anytype.presentation.editor.editor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType

sealed class ObjectTypeMenuItem {
    object ChangeType: ObjectTypeMenuItem()
    data class OpenSet(val set: Id, val space: Id, val typeName: String) : ObjectTypeMenuItem()
    data class CreateSet(val type: Id, val typeName: String) : ObjectTypeMenuItem()
}

fun ListenerType.Relation.ObjectType.items(
    set: Id? = null,
    space: Id
): List<ObjectTypeMenuItem> {
    val relation = this
    return if (set.isNullOrBlank()) {
        listOf(
            ObjectTypeMenuItem.ChangeType,
            ObjectTypeMenuItem.CreateSet(
                type = relation.relation.id,
                typeName = relation.relation.name
            )
        )
    } else {
        listOf(
            ObjectTypeMenuItem.ChangeType,
            ObjectTypeMenuItem.OpenSet(
                set = set,
                space = space,
                typeName = relation.relation.name
            )
        )
    }
}
