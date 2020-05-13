package com.agileburo.anytype.presentation.page.editor

import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.domain.block.model.Block

sealed class ViewState {
    object Loading : ViewState()
    data class Success(val blocks: List<BlockView>) : ViewState()
    data class Error(val message: String) : ViewState()
    data class OpenLinkScreen(
        val pageId: String,
        val block: Block,
        val range: IntRange
    ) : ViewState()
}