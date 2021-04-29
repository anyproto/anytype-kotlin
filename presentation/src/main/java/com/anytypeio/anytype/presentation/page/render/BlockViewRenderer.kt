package com.anytypeio.anytype.presentation.page.render

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.page.Editor.Mode as EditorMode

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
        mode: EditorMode = EditorMode.Edit,
        root: Block,
        focus: Editor.Focus,
        anchor: Id,
        indent: Int,
        details: Block.Details = Block.Details(emptyMap()),
        relations: List<Relation>
    ): List<BlockView>
}