package com.anytypeio.anytype.core_ui.features.page.scrollandmove

import com.anytypeio.anytype.presentation.page.editor.sam.ScrollAndMoveTarget
import com.anytypeio.anytype.presentation.page.editor.sam.ScrollAndMoveTargetDescriptor


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