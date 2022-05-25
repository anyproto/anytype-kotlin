package com.anytypeio.anytype.core_ui.tools


interface SupportDragAndDropBehavior {
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
}