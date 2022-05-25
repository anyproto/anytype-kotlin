package com.anytypeio.anytype.core_ui.features.editor.scrollandmove

import com.anytypeio.anytype.presentation.editor.editor.sam.ScrollAndMoveTarget
import com.anytypeio.anytype.presentation.editor.editor.sam.ScrollAndMoveTargetDescriptor


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