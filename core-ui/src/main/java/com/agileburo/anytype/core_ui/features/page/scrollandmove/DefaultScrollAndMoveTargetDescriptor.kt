package com.agileburo.anytype.core_ui.features.page.scrollandmove


class DefaultScrollAndMoveTargetDescriptor : ScrollAndMoveTargetDescriptor {

    var target: ScrollAndMoveTarget? = null

    override fun clear() {
        target = null
    }

    override fun current(): ScrollAndMoveTarget? = target

    override fun update(target: ScrollAndMoveTarget) {
        this.target = target
    }
}