package com.agileburo.anytype.presentation.page.render

import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_utils.tools.Counter
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Block.Content
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.emoji.Emojifier
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.presentation.mapper.marks
import com.agileburo.anytype.presentation.mapper.toFileView
import com.agileburo.anytype.presentation.mapper.toPictureView
import com.agileburo.anytype.presentation.mapper.toVideoView
import com.agileburo.anytype.presentation.page.toggle.ToggleStateHolder

class DefaultBlockViewRenderer(
    private val urlBuilder: UrlBuilder,
    private val emojifier: Emojifier,
    private val toggleStateHolder: ToggleStateHolder
) : BlockViewRenderer, ToggleStateHolder by toggleStateHolder {

    override suspend fun Map<Id, List<Block>>.render(
        root: Block,
        focus: Id,
        anchor: Id,
        indent: Int,
        counter: Counter,
        details: Block.Details
    ): List<BlockView> {

        val children = getValue(anchor)

        val result = mutableListOf<BlockView>()

        buildTitle(anchor, root, result, details, focus)

        counter.reset()

        children.forEach { block ->
            when (val content = block.content) {
                is Content.Text -> {
                    when (content.style) {
                        Content.Text.Style.TITLE -> {
                            counter.reset()
                            result.add(title(block, content, root, focus))
                        }
                        Content.Text.Style.P -> {
                            counter.reset()
                            result.add(paragraph(block, content, focus, indent))
                        }
                        Content.Text.Style.NUMBERED -> {
                            counter.inc()
                            result.add(numbered(block, content, counter.current(), focus, indent))
                        }
                        Content.Text.Style.TOGGLE -> {
                            counter.reset()
                            result.add(
                                toggle(
                                    block = block,
                                    content = content,
                                    indent = indent,
                                    isEmpty = block.children.isEmpty(),
                                    focus = focus
                                )
                            )
                            if (toggleStateHolder.isToggled(block.id)) {
                                result.addAll(
                                    render(
                                        focus = focus,
                                        indent = indent.inc(),
                                        anchor = block.id,
                                        root = root,
                                        counter = counter,
                                        details = details
                                    )
                                )
                            }
                        }
                        Content.Text.Style.H1 -> {
                            counter.reset()
                            result.add(headerOne(block, content, indent))
                        }
                        Content.Text.Style.H2 -> {
                            counter.reset()
                            result.add(headerTwo(block, content, indent))
                        }
                        Content.Text.Style.H3, Content.Text.Style.H4 -> {
                            counter.reset()
                            result.add(headerThree(block, content, indent))
                        }
                        Content.Text.Style.QUOTE -> {
                            counter.reset()
                            result.add(highlight(block, content, indent))
                        }
                        Content.Text.Style.CODE_SNIPPET -> {
                            counter.reset()
                            result.add(code(block, content))
                        }
                        Content.Text.Style.BULLET -> {
                            counter.reset()
                            result.add(bulleted(block, content, focus, indent))
                        }
                        Content.Text.Style.CHECKBOX -> {
                            counter.reset()
                            result.add(checkbox(block, content, focus, indent))
                        }
                    }
                }
                is Content.Bookmark -> {
                    counter.reset()
                    result.add(bookmark(content, block, indent))
                }
                is Content.Divider -> {
                    counter.reset()
                    result.add(divider(block))
                }
                is Content.Link -> {
                    counter.reset()
                    result.add(page(block, content, indent))
                }
                is Content.File -> {
                    counter.reset()
                    result.add(file(content, block, indent))
                }
            }
        }

        return result
    }

    private suspend fun buildTitle(
        anchor: Id,
        root: Block,
        result: MutableList<BlockView>,
        details: Block.Details,
        focus: Id
    ) {
        if (anchor == root.id) {
            result.add(
                BlockView.Title(
                    id = anchor,
                    focused = anchor == focus,
                    text = details.details[root.id]?.name,
                    emoji = details.details[root.id]?.icon?.let { name ->
                        if (name.isNotEmpty())
                            emojifier.fromShortName(name).unicode
                        else
                            null
                    }
                )
            )
        }
    }

    private fun paragraph(
        block: Block,
        content: Content.Text,
        focus: Id,
        indent: Int
    ): BlockView.Paragraph = BlockView.Paragraph(
        id = block.id,
        text = content.text,
        marks = content.marks(),
        focused = block.id == focus,
        color = content.color,
        backgroundColor = content.backgroundColor,
        indent = indent
    )

    private fun headerThree(
        block: Block,
        content: Content.Text,
        indent: Int
    ): BlockView.HeaderThree = BlockView.HeaderThree(
        id = block.id,
        text = content.text,
        color = content.color,
        backgroundColor = content.backgroundColor,
        indent = indent
    )

    private fun headerTwo(
        block: Block,
        content: Content.Text,
        indent: Int
    ): BlockView.HeaderTwo = BlockView.HeaderTwo(
        id = block.id,
        text = content.text,
        color = content.color,
        backgroundColor = content.backgroundColor,
        indent = indent
    )

    private fun headerOne(
        block: Block,
        content: Content.Text,
        indent: Int
    ): BlockView.HeaderOne = BlockView.HeaderOne(
        id = block.id,
        text = content.text,
        color = content.color,
        backgroundColor = content.backgroundColor,
        indent = indent
    )

    private fun checkbox(
        block: Block,
        content: Content.Text,
        focus: Id,
        indent: Int
    ): BlockView.Checkbox = BlockView.Checkbox(
        id = block.id,
        text = content.text,
        marks = content.marks(),
        isChecked = content.isChecked == true,
        color = content.color,
        backgroundColor = content.backgroundColor,
        focused = block.id == focus,
        indent = indent
    )

    private fun bulleted(
        block: Block,
        content: Content.Text,
        focus: Id,
        indent: Int
    ): BlockView.Bulleted = BlockView.Bulleted(
        id = block.id,
        text = content.text,
        indent = indent,
        marks = content.marks(),
        focused = block.id == focus,
        color = content.color,
        backgroundColor = content.backgroundColor
    )

    private fun code(
        block: Block,
        content: Content.Text
    ): BlockView.Code = BlockView.Code(
        id = block.id,
        snippet = content.text
    )

    private fun highlight(
        block: Block,
        content: Content.Text,
        indent: Int
    ): BlockView.Highlight = BlockView.Highlight(
        id = block.id,
        text = content.text,
        indent = indent
    )

    private fun toggle(
        block: Block,
        content: Content.Text,
        indent: Int,
        focus: Id,
        isEmpty: Boolean
    ): BlockView.Toggle = BlockView.Toggle(
        id = block.id,
        text = content.text,
        marks = content.marks(),
        color = content.color,
        backgroundColor = content.backgroundColor,
        indent = indent,
        focused = block.id == focus,
        toggled = toggleStateHolder.isToggled(block.id),
        isEmpty = isEmpty
    )

    private fun numbered(
        block: Block,
        content: Content.Text,
        number: Int,
        focus: Id,
        indent: Int
    ): BlockView.Numbered = BlockView.Numbered(
        id = block.id,
        text = content.text,
        number = number,
        focused = block.id == focus,
        color = content.color,
        backgroundColor = content.backgroundColor,
        indent = indent,
        marks = content.marks()
    )

    private fun bookmark(
        content: Content.Bookmark,
        block: Block,
        indent: Int
    ): BlockView = content.url?.let { url ->
        if (content.title != null && content.description != null) {
            BlockView.Bookmark.View(
                id = block.id,
                url = url,
                title = content.title,
                description = content.description,
                imageUrl = content.image?.let { urlBuilder.image(it) },
                faviconUrl = content.favicon?.let { urlBuilder.image(it) },
                indent = indent
            )
        } else {
            BlockView.Bookmark.Error(
                id = block.id,
                url = url,
                indent = indent
            )
        }
    } ?: BlockView.Bookmark.Placeholder(
        id = block.id,
        indent = indent
    )

    private fun divider(block: Block) = BlockView.Divider(id = block.id)

    private fun file(
        content: Content.File,
        block: Block,
        indent: Int
    ): BlockView = when (content.type) {
        Content.File.Type.IMAGE -> content.toPictureView(
            id = block.id,
            urlBuilder = urlBuilder,
            indent = indent
        )
        Content.File.Type.FILE -> content.toFileView(
            id = block.id,
            urlBuilder = urlBuilder,
            indent = indent
        )
        Content.File.Type.VIDEO -> content.toVideoView(
            id = block.id,
            urlBuilder = urlBuilder,
            indent = indent
        )
        else -> throw IllegalStateException("Unexpected file type: ${content.type}")
    }

    private suspend fun title(
        block: Block,
        content: Content.Text,
        root: Block,
        focus: Id
    ): BlockView.Title = BlockView.Title(
        id = block.id,
        text = content.text,
        emoji = root.fields.icon?.let { name ->
            if (name.isNotEmpty())
                emojifier.fromShortName(name).unicode
            else
                null
        },
        focused = block.id == focus
    )

    private fun page(
        block: Block,
        content: Content.Link,
        indent: Int
    ): BlockView.Page = BlockView.Page(
        id = block.id,
        isEmpty = true,
        emoji = null,
        text = if (content.fields.hasName()) content.fields.name else null,
        indent = indent
    )
}