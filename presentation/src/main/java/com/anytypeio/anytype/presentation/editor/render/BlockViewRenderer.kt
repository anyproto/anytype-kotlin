package com.anytypeio.anytype.presentation.editor.render

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.AllObjectsDetails
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.Editor.Mode as EditorMode

/**
 * Converts business tree-like data structures to flattened view data structures.
 */
interface BlockViewRenderer {

    /**
     * Ext. function for recursively converting map to flattened view data structure.
     * @param context object id
     * @param root root block, from which rendering starts
     * @param focus id of the current focus
     * @param anchor id of the current anchor (current rendering node)
     * @param indent current indent at this rendering node.
     */
    suspend fun Map<Id, List<Block>>.render(
        context: Id,
        mode: EditorMode = EditorMode.Edit,
        root: Block,
        focus: Editor.Focus,
        anchor: Id,
        indent: Int,
        details: AllObjectsDetails,
        restrictions: List<ObjectRestriction>,
        selection: Set<Id>,
        count: Int = 0,
        parentScheme: NestedDecorationData = emptyList(),
        onRenderFlag: (RenderFlag) -> Unit = {},
    ): List<BlockView>

    sealed class RenderFlag {
        object ContainsTableOfContents: RenderFlag()
    }
}