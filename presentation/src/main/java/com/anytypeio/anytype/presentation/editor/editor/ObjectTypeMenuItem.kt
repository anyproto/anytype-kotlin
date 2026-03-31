package com.anytypeio.anytype.presentation.editor.editor

sealed class ObjectTypeMenuItem {
    object ChangeType: ObjectTypeMenuItem()
    object OpenType: ObjectTypeMenuItem()
}
