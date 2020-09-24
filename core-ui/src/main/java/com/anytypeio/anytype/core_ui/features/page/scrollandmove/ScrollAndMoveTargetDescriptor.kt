package com.anytypeio.anytype.core_ui.features.page.scrollandmove

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