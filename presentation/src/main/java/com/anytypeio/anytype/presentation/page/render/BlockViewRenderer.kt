package com.anytypeio.anytype.presentation.page.render

import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.domain.page.EditorMode
import com.anytypeio.anytype.presentation.page.editor.model.BlockView

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