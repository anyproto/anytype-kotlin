package com.anytypeio.anytype.core_ui.features.editor.decoration

import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

interface DecoratableViewHolder {
    val decoratableContainer: EditorDecorationContainer
    fun applyDecorations(decorations: List<BlockView.Decoration>)
    fun onDecorationsChanged(decorations: List<BlockView.Decoration>)
}