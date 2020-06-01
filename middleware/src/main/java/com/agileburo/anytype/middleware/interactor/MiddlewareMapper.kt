package com.agileburo.anytype.middleware.interactor

import anytype.Events
import anytype.model.Models.Block
import com.agileburo.anytype.data.auth.model.BlockEntity
import com.agileburo.anytype.data.auth.model.PayloadEntity
import com.agileburo.anytype.data.auth.model.PositionEntity
import com.agileburo.anytype.middleware.converters.block
import com.agileburo.anytype.middleware.converters.blocks
import com.agileburo.anytype.middleware.converters.toMiddleware

class MiddlewareMapper {

    fun toMiddleware(blocks: List<BlockEntity>) : List<Block> {
        return blocks.map { it.block() }
    }

    fun toMiddleware(style: BlockEntity.Content.Text.Style): Block.Content.Text.Style {
        return style.toMiddleware()
    }

    fun toMiddleware(position: PositionEntity): Block.Position {
        return position.toMiddleware()
    }

    fun toPayload(response: Events.ResponseEvent): PayloadEntity {

        val context = response.contextId

        return PayloadEntity(
            context = context,
            events = response.messagesList.mapNotNull { it.toEntity(context) }
        )
    }

    fun toMiddleware(alignment: BlockEntity.Align): Block.Align {
        return alignment.toMiddleware()
    }

    fun toEntity(blocks: List<Block>) : List<BlockEntity> {
        return blocks.blocks()
    }
}