package com.anytypeio.anytype.presentation.page.editor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.presentation.page.editor.model.BlockView

sealed class ViewState {
    object Loading : ViewState()
    data class Success(val blocks: List<BlockView>) : ViewState()
    data class OpenLinkScreen(
        val pageId: String,
        val block: Block,
        val range: IntRange
    ) : ViewState()
}