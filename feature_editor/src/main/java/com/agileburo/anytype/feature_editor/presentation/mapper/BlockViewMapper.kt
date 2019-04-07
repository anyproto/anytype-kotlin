package com.agileburo.anytype.feature_editor.presentation.mapper

import com.agileburo.anytype.feature_editor.domain.Block
import com.agileburo.anytype.feature_editor.domain.Content
import com.agileburo.anytype.feature_editor.presentation.model.BlockView

class BlockViewMapper : ViewMapper<Block, BlockView> {

    override fun mapToView(model: Block): BlockView {
        return BlockView(
            id = model.id,
            contentType = model.contentType,
            content = mapTextContent(model.content)
        )
    }

    private fun mapTextContent(content: Content.Text): BlockView.Content.Text {
        return BlockView.Content.Text(
            text = content.text,
            marks = content.marks,
            param = BlockView.ContentParam(
                mapOf(
                    "number" to content.param.number,
                    "checked" to content.param.checked
                )
            )
        )
    }
}