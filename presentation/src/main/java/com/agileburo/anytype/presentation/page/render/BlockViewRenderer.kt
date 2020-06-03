package com.agileburo.anytype.presentation.page.render

import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.editor.Editor
import com.agileburo.anytype.domain.page.EditorMode

/**
 * Converts business tree-like data structures to flattened view data structures.
 */
interface BlockViewRenderer {

    /**
     * Ext. function for recursively converting map to flattened view data structure.
     * @param root root block, from which rendering starts
     * @param focus id of the current focus
     * @param anchor id of the current anchor (current rendering node)
     * @param indent current indent at this rendering node.
     */
    suspend fun Map<Id, List<Block>>.render(
        mode: EditorMode = EditorMode.EDITING,
        root: Block,
        focus: Editor.Focus,
        anchor: Id,
        indent: Int,
        details: Block.Details = Block.Details(emptyMap())
    ): List<BlockView>
}