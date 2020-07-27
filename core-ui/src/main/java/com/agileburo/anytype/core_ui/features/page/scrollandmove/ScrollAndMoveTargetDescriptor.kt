package com.agileburo.anytype.core_ui.features.page.scrollandmove

interface ScrollAndMoveTargetDescriptor {

    fun clear()
    fun update(target: ScrollAndMoveTarget)
    fun current(): ScrollAndMoveTarget?

    companion object {
        val START_RANGE = 0.0..0.2
        val END_RANGE = 0.8..1.0
    }
}