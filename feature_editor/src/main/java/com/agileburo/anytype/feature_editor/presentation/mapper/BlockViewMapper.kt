package com.agileburo.anytype.feature_editor.presentation.mapper

import com.agileburo.anytype.feature_editor.domain.Block
import com.agileburo.anytype.feature_editor.presentation.model.BlockView

class BlockViewMapper : ViewMapper<Block, BlockView> {

    override fun mapToView(model: Block): BlockView {
        return BlockView(
            id = model.id,
            contentType = model.contentType,
            content = model.content
        )
    }
}