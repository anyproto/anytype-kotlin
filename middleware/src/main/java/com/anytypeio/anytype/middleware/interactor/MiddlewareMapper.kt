package com.anytypeio.anytype.middleware.interactor

import anytype.ResponseEvent
import anytype.Rpc
import anytype.model.Block
import com.anytypeio.anytype.data.auth.model.BlockEntity
import com.anytypeio.anytype.data.auth.model.PayloadEntity
import com.anytypeio.anytype.data.auth.model.PositionEntity
import com.anytypeio.anytype.middleware.converters.block
import com.anytypeio.anytype.middleware.converters.blocks
import com.anytypeio.anytype.middleware.converters.toMiddleware

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

    fun toMiddleware(mode: BlockEntity.Content.Text.SplitMode): Rpc.Block.Split.Request.Mode {
        return when (mode) {
            BlockEntity.Content.Text.SplitMode.BOTTOM -> Rpc.Block.Split.Request.Mode.BOTTOM
            BlockEntity.Content.Text.SplitMode.TOP -> Rpc.Block.Split.Request.Mode.TOP
            BlockEntity.Content.Text.SplitMode.INNER -> Rpc.Block.Split.Request.Mode.INNER
        }
    }

    fun toPayload(response: ResponseEvent?): PayloadEntity {

        checkNotNull(response)

        val context = response.contextId

        return PayloadEntity(
            context = context,
            events = response.messages.mapNotNull { it.toEntity(context) }
        )
    }

    fun toMiddleware(alignment: BlockEntity.Align): Block.Align {
        return alignment.toMiddleware()
    }

    fun toEntity(blocks: List<Block>): List<BlockEntity> {
        return blocks.blocks()
    }
}