package com.anytypeio.anytype.middleware.interactor

import anytype.model.Models.Block
import com.anytypeio.anytype.data.auth.model.BlockEntity
import com.anytypeio.anytype.middleware.converters.state
import com.anytypeio.anytype.middleware.converters.toMiddleware
import com.anytypeio.anytype.middleware.converters.type

class MiddlewareFactory {

    fun create(prototype: BlockEntity.Prototype): Block {

        val builder = Block.newBuilder()

        return when (prototype) {
            is BlockEntity.Prototype.Bookmark -> {
                val bookmark = Block.Content.Bookmark.getDefaultInstance()
                builder.setBookmark(bookmark).build()
            }
            is BlockEntity.Prototype.Text -> {
                val text = Block.Content.Text.newBuilder().apply {
                    style = prototype.style.toMiddleware()
                }
                builder.setText(text).build()
            }
            is BlockEntity.Prototype.DividerLine -> {
                val divider = Block.Content.Div.newBuilder().apply {
                    style = Block.Content.Div.Style.Line
                }
                builder.setDiv(divider).build()
            }
            is BlockEntity.Prototype.DividerDots -> {
                val divider = Block.Content.Div.newBuilder().apply {
                    style = Block.Content.Div.Style.Dots
                }
                builder.setDiv(divider).build()
            }
            is BlockEntity.Prototype.File -> {
                val file = Block.Content.File.newBuilder().apply {
                    state = prototype.state.state()
                    type = prototype.type.type()
                }
                builder.setFile(file).build()
            }
            is BlockEntity.Prototype.Link -> {
                val link = Block.Content.Link.newBuilder().apply {
                    targetBlockId = prototype.target
                }
                builder.setLink(link).build()
            }
            else -> throw IllegalStateException("Unexpected prototype: $prototype")
        }
    }
}