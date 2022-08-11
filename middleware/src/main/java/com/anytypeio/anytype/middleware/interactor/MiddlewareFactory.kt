package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.middleware.mappers.MBBookmark
import com.anytypeio.anytype.middleware.mappers.MBDiv
import com.anytypeio.anytype.middleware.mappers.MBDivStyle
import com.anytypeio.anytype.middleware.mappers.MBFile
import com.anytypeio.anytype.middleware.mappers.MBLink
import com.anytypeio.anytype.middleware.mappers.MBRelation
import com.anytypeio.anytype.middleware.mappers.MBTableOfContents
import com.anytypeio.anytype.middleware.mappers.MBText
import com.anytypeio.anytype.middleware.mappers.MBlock
import com.anytypeio.anytype.middleware.mappers.MBookmarkState
import com.anytypeio.anytype.middleware.mappers.toMiddlewareModel

class MiddlewareFactory {

    fun create(prototype: Block.Prototype): MBlock {
        return when (prototype) {
            is Block.Prototype.Bookmark.New -> {
                val bookmark = MBBookmark()
                MBlock(bookmark = bookmark)
            }
            is Block.Prototype.Bookmark.Existing -> {
                val bookmark = MBBookmark(
                    targetObjectId = prototype.target,
                    state = MBookmarkState.Done
                )
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
                    targetBlockId = prototype.target,
                    cardStyle = prototype.cardStyle.toMiddlewareModel(),
                    iconSize = prototype.iconSize.toMiddlewareModel(),
                    description = prototype.description.toMiddlewareModel()
                )
                MBlock(link = link)
            }
            is Block.Prototype.Relation -> {
                val relation = MBRelation(
                    key = prototype.key
                )
                MBlock(relation = relation)
            }
            is Block.Prototype.TableOfContents -> {
                val toc = MBTableOfContents()
                MBlock(tableOfContents = toc)
            }
            else -> throw IllegalStateException("Unexpected prototype: $prototype")
        }
    }
}