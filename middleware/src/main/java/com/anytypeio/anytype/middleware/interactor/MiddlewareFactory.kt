package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.middleware.mappers.*

class MiddlewareFactory {

    fun create(prototype: Block.Prototype): MBlock {
        return when (prototype) {
            is Block.Prototype.Bookmark -> {
                val bookmark = MBBookmark()
                MBlock(bookmark = bookmark)
            }
            is Block.Prototype.Text -> {
                val text = MBText(style = prototype.style.toMiddlewareModel())
                MBlock(text = text)
            }
            is Block.Prototype.DividerLine -> {
                val divider = MBDiv(style = MBDivStyle.Line)
                MBlock(div = divider)
            }
            is Block.Prototype.DividerDots -> {
                val divider = MBDiv(style = MBDivStyle.Dots)
                MBlock(div = divider)
            }
            is Block.Prototype.File -> {
                val file = MBFile(
                    state = prototype.state.toMiddlewareModel(),
                    type = prototype.type.toMiddlewareModel()
                )
                MBlock(file_ = file)
            }
            is Block.Prototype.Link -> {
                val link = MBLink(
                    targetBlockId = prototype.target
                )
                MBlock(link = link)
            }
            is Block.Prototype.Relation -> {
                val relation = MBRelation(
                    key = prototype.key
                )
                MBlock(relation = relation)
            }
            else -> throw IllegalStateException("Unexpected prototype: $prototype")
        }
    }
}