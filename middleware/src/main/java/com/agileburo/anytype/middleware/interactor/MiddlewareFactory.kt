package com.agileburo.anytype.middleware.interactor

import anytype.model.Models.Block
import com.agileburo.anytype.data.auth.model.BlockEntity
import com.agileburo.anytype.middleware.converters.state
import com.agileburo.anytype.middleware.converters.toMiddleware
import com.agileburo.anytype.middleware.converters.type

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
            is BlockEntity.Prototype.Divider -> {
                val divider = Block.Content.Div.newBuilder().apply {
                    style = Block.Content.Div.Style.Line
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
            else -> throw IllegalStateException("Unexpected prototype: $prototype")
        }
    }
}