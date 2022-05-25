package com.anytypeio.anytype.presentation.editor.editor.sam

interface ScrollAndMoveTargetDescriptor {

    fun clear()
    fun update(target: ScrollAndMoveTarget)
    fun current(): ScrollAndMoveTarget?

    companion object {
        val START_RANGE = 0.0..0.25
        val END_RANGE = 0.75..1.0
        val INNER_RANGE = 0.25..0.75
    }
}