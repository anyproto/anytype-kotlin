package com.anytypeio.anytype.middleware.interactor

import anytype.model.Block
import com.anytypeio.anytype.data.auth.model.BlockEntity
import com.anytypeio.anytype.middleware.converters.*

class MiddlewareFactory {

    fun create(prototype: BlockEntity.Prototype): Block {
        return when (prototype) {
            is BlockEntity.Prototype.Bookmark -> {
                Block(bookmark = Bookmark())
            }
            is BlockEntity.Prototype.Text -> {
                val text = Block.Content.Text(style = prototype.style.toMiddleware())
                Block(text = text)
            }
            is BlockEntity.Prototype.DividerLine -> {
                val divider = Block.Content.Div(style = Block.Content.Div.Style.Line)
                Block(div = divider)
            }
            is BlockEntity.Prototype.DividerDots -> {
                val divider = Block.Content.Div(style = Block.Content.Div.Style.Dots)
                Block(div = divider)
            }
            is BlockEntity.Prototype.File -> {
                val file = File(
                    state = prototype.state.state(),
                    type = prototype.type.type()
                )
                Block(file_ = file)
            }
            is BlockEntity.Prototype.Link -> {
                val link = Block.Content.Link(
                    targetBlockId = prototype.target
                )
                Block(link = link)
            }
            else -> throw IllegalStateException("Unexpected prototype: $prototype")
        }
    }
}