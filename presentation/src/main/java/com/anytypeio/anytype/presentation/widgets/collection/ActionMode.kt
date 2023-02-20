package com.anytypeio.anytype.presentation.widgets.collection

sealed class ActionMode {
    object SelectAll : ActionMode()
    object UnselectAll : ActionMode()
    object Edit : ActionMode()
    object Done : ActionMode()
}