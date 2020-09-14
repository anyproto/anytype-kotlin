package com.agileburo.anytype.presentation.page.render

import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_utils.tools.Counter
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Block.Content
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.editor.Editor.Cursor
import com.agileburo.anytype.domain.editor.Editor.Focus
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.EditorMode
import com.agileburo.anytype.presentation.mapper.*
import com.agileburo.anytype.presentation.page.toggle.ToggleStateHolder

class DefaultBlockViewRenderer(
    private val counter: Counter,
    private val urlBuilder: UrlBuilder,
    private val toggleStateHolder: ToggleStateHolder
) : BlockViewRenderer, ToggleStateHolder by toggleStateHolder {

    override suspend fun Map<Id, List<Block>>.render(
        mode: EditorMode,
        root: Block,
        focus: Focus,
        anchor: Id,
        indent: Int,
        details: Block.Details
    ): List<BlockView> {

        val children = getValue(anchor)

        val result = mutableListOf<BlockView>()

        buildTitle(
            mode = mode,
            anchor = anchor,
            root = root,
            result = result,
            details = details,
            focus = focus
        )

        counter.reset()

        children.forEach { block ->
            when (val content = block.content) {
                is Content.Text -> {
                    when (content.style) {
                        Content.Text.Style.TITLE -> {
                            counter.reset()
                            result.add(title(mode, block, content, root, focus))
                        }
                        Content.Text.Style.P -> {
                            counter.reset()
                            result.add(
                                paragraph(
                                    mode = mode,
                                    block = block,
                                    content = content,
                                    focus = focus,
                                    indent = indent,
                                    details = details
                                )
                            )
                            if (block.children.isNotEmpty()) {
                                result.addAll(
                                    render(
                                        mode = mode,
                                        focus = focus,
                                        indent = indent.inc(),
                                        anchor = block.id,
                                        root = root,
                                        details = details
                                    )
                                )
                            }
                        }
                        Content.Text.Style.NUMBERED -> {
                            counter.inc()
                            result.add(
                                numbered(
                                    mode,
                                    block,
                                    content,
                                    counter.current(),
                                    focus,
                                    indent
                                )
                            )
                            if (block.children.isNotEmpty()) {
                                result.addAll(
                                    render(
                                        mode = mode,
                                        focus = focus,
                                        indent = indent.inc(),
                                        anchor = block.id,
                                        root = root,
                                        details = details
                                    )
                                )
                            }
                        }
                        Content.Text.Style.TOGGLE -> {
                            counter.reset()
                            result.add(
                                toggle(
                                    mode = mode,
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
                                        mode = mode,
                                        focus = focus,
                                        indent = indent.inc(),
                                        anchor = block.id,
                                        root = root,
                                        details = details
                                    )
                                )
                            }
                        }
                        Content.Text.Style.H1 -> {
                            counter.reset()
                            result.add(headerOne(mode, block, focus, content, indent))
                            if (block.children.isNotEmpty()) {
                                result.addAll(
                                    render(
                                        mode = mode,
                                        focus = focus,
                                        indent = indent.inc(),
                                        anchor = block.id,
                                        root = root,
                                        details = details
                                    )
                                )
                            }
                        }
                        Content.Text.Style.H2 -> {
                            counter.reset()
                            result.add(headerTwo(mode, block, focus, content, indent))
                            if (block.children.isNotEmpty()) {
                                result.addAll(
                                    render(
                                        mode = mode,
                                        focus = focus,
                                        indent = indent.inc(),
                                        anchor = block.id,
                                        root = root,
                                        details = details
                                    )
                                )
                            }
                        }
                        Content.Text.Style.H3, Content.Text.Style.H4 -> {
                            counter.reset()
                            result.add(headerThree(mode, block, focus, content, indent))
                            if (block.children.isNotEmpty()) {
                                result.addAll(
                                    render(
                                        mode = mode,
                                        focus = focus,
                                        indent = indent.inc(),
                                        anchor = block.id,
                                        root = root,
                                        details = details
                                    )
                                )
                            }
                        }
                        Content.Text.Style.QUOTE -> {
                            counter.reset()
                            result.add(highlight(mode, block, focus, content, indent))
                            if (block.children.isNotEmpty()) {
                                result.addAll(
                                    render(
                                        mode = mode,
                                        focus = focus,
                                        indent = indent.inc(),
                                        anchor = block.id,
                                        root = root,
                                        details = details
                                    )
                                )
                            }
                        }
                        Content.Text.Style.BULLET -> {
                            counter.reset()
                            result.add(bulleted(mode, block, content, focus, indent))
                            if (block.children.isNotEmpty()) {
                                result.addAll(
                                    render(
                                        mode = mode,
                                        focus = focus,
                                        indent = indent.inc(),
                                        anchor = block.id,
                                        root = root,
                                        details = details
                                    )
                                )
                            }
                        }
                        Content.Text.Style.CHECKBOX -> {
                            counter.reset()
                            result.add(checkbox(mode, block, content, focus, indent))
                            if (block.children.isNotEmpty()) {
                                result.addAll(
                                    render(
                                        mode = mode,
                                        focus = focus,
                                        indent = indent.inc(),
                                        anchor = block.id,
                                        root = root,
                                        details = details
                                    )
                                )
                            }
                        }
                        Content.Text.Style.CODE_SNIPPET -> {
                            counter.reset()
                            result.add(code(mode, block, content, focus))
                            if (block.children.isNotEmpty()) {
                                result.addAll(
                                    render(
                                        mode = mode,
                                        focus = focus,
                                        indent = indent.inc(),
                                        anchor = block.id,
                                        root = root,
                                        details = details
                                    )
                                )
                            }
                        }
                    }
                }
                is Content.Bookmark -> {
                    counter.reset()
                    result.add(bookmark(mode, content, block, indent))
                }
                is Content.Divider -> {
                    counter.reset()
                    result.add(divider(block, indent))
                }
                is Content.Link -> {
                    counter.reset()
                    result.add(page(block, content, indent, details))
                }
                is Content.File -> {
                    counter.reset()
                    result.add(file(mode, content, block, indent))
                }
                is Content.Layout -> {
                    counter.reset()
                    result.addAll(
                        render(
                            mode = mode,
                            focus = focus,
                            indent = indent,
                            anchor = block.id,
                            root = root,
                            details = details
                        )
                    )
                }
            }
        }

        return result
    }

    private fun buildTitle(
        mode: EditorMode,
        anchor: Id,
        root: Block,
        result: MutableList<BlockView>,
        details: Block.Details,
        focus: Focus
    ) {
        if (anchor == root.id) {

            val cursor = if (anchor == focus.id) {
                focus.cursor?.let { cursor ->
                    when (cursor) {
                        is Cursor.Start -> 0
                        is Cursor.End -> details.details[root.id]?.name?.length ?: 0
                        is Cursor.Range -> cursor.range.first
                    }
                }
            } else {
                null
            }
            val viewMode =
                if (mode == EditorMode.EDITING)
                    BlockView.Mode.EDIT
                else
                    BlockView.Mode.READ
            val text = details.details[root.id]?.name
            val isFocused = anchor == focus.id
            val type = (root.content as? Content.Smart)?.type
            result.add(
                when (type) {
                    Content.Smart.Type.PROFILE -> {
                        BlockView.Title.Profile(
                            mode = viewMode,
                            id = anchor,
                            isFocused = isFocused,
                            text = text,
                            image = details.details[root.id]?.iconImage?.let { name ->
                                if (name.isNotEmpty()) urlBuilder.thumbnail(name) else null
                            },
                            cursor = cursor
                        )
                    }
                    Content.Smart.Type.ARCHIVE -> {
                        BlockView.Title.Archive(
                            mode = viewMode,
                            id = anchor,
                            text = text
                        )
                    }
                    else -> {
                        BlockView.Title.Document(
                            mode = viewMode,
                            id = anchor,
                            isFocused = isFocused,
                            text = text,
                            emoji = details.details[root.id]?.iconEmoji?.let { name ->
                                if (name.isNotEmpty())
                                    name
                                else
                                    null
                            },
                            image = details.details[root.id]?.iconImage?.let { name ->
                                if (name.isNotEmpty())
                                    urlBuilder.thumbnail(name)
                                else
                                    null
                            },
                            cursor = cursor
                        )
                    }

                }
            )
        }
    }

    private fun paragraph(
        mode: EditorMode,
        block: Block,
        content: Content.Text,
        focus: Focus,
        indent: Int,
        details: Block.Details
    ): BlockView.Text.Paragraph = BlockView.Text.Paragraph(
        mode = if (mode == EditorMode.EDITING) BlockView.Mode.EDIT else BlockView.Mode.READ,
        id = block.id,
        text = content.text,
        marks = content.marks(details = details, urlBuilder = urlBuilder),
        isFocused = block.id == focus.id,
        color = content.color,
        backgroundColor = content.backgroundColor,
        indent = indent,
        alignment = content.align?.toView(),
        cursor = if (block.id == focus.id) setCursor(focus, content) else null
    )

    private fun headerThree(
        mode: EditorMode,
        block: Block,
        focus: Focus,
        content: Content.Text,
        indent: Int
    ): BlockView.Text.Header.Three = BlockView.Text.Header.Three(
        mode = if (mode == EditorMode.EDITING) BlockView.Mode.EDIT else BlockView.Mode.READ,
        id = block.id,
        text = content.text,
        color = content.color,
        isFocused = block.id == focus.id,
        marks = content.marks(),
        backgroundColor = content.backgroundColor,
        indent = indent,
        alignment = content.align?.toView(),
        cursor = if (block.id == focus.id) setCursor(focus, content) else null
    )

    private fun headerTwo(
        mode: EditorMode,
        block: Block,
        focus: Focus,
        content: Content.Text,
        indent: Int
    ): BlockView.Text.Header.Two = BlockView.Text.Header.Two(
        mode = if (mode == EditorMode.EDITING) BlockView.Mode.EDIT else BlockView.Mode.READ,
        id = block.id,
        text = content.text,
        color = content.color,
        isFocused = block.id == focus.id,
        marks = content.marks(),
        backgroundColor = content.backgroundColor,
        indent = indent,
        alignment = content.align?.toView(),
        cursor = if (block.id == focus.id) setCursor(focus, content) else null
    )

    private fun headerOne(
        mode: EditorMode,
        block: Block,
        focus: Focus,
        content: Content.Text,
        indent: Int
    ): BlockView.Text.Header.One = BlockView.Text.Header.One(
        mode = if (mode == EditorMode.EDITING) BlockView.Mode.EDIT else BlockView.Mode.READ,
        id = block.id,
        text = content.text,
        color = content.color,
        isFocused = block.id == focus.id,
        marks = content.marks(),
        backgroundColor = content.backgroundColor,
        indent = indent,
        alignment = content.align?.toView(),
        cursor = if (block.id == focus.id) setCursor(focus, content) else null
    )

    private fun checkbox(
        mode: EditorMode,
        block: Block,
        content: Content.Text,
        focus: Focus,
        indent: Int
    ): BlockView.Text.Checkbox = BlockView.Text.Checkbox(
        mode = if (mode == EditorMode.EDITING) BlockView.Mode.EDIT else BlockView.Mode.READ,
        id = block.id,
        text = content.text,
        marks = content.marks(),
        isChecked = content.isChecked == true,
        color = content.color,
        backgroundColor = content.backgroundColor,
        isFocused = block.id == focus.id,
        indent = indent,
        cursor = if (block.id == focus.id) setCursor(focus, content) else null
    )

    private fun bulleted(
        mode: EditorMode,
        block: Block,
        content: Content.Text,
        focus: Focus,
        indent: Int
    ): BlockView.Text.Bulleted = BlockView.Text.Bulleted(
        mode = if (mode == EditorMode.EDITING) BlockView.Mode.EDIT else BlockView.Mode.READ,
        id = block.id,
        text = content.text,
        indent = indent,
        marks = content.marks(),
        isFocused = block.id == focus.id,
        color = content.color,
        backgroundColor = content.backgroundColor,
        cursor = if (block.id == focus.id) setCursor(focus, content) else null
    )

    private fun code(
        mode: EditorMode,
        block: Block,
        content: Content.Text,
        focus: Focus
    ): BlockView.Code = BlockView.Code(
        mode = if (mode == EditorMode.EDITING) BlockView.Mode.EDIT else BlockView.Mode.READ,
        id = block.id,
        text = content.text,
        isFocused = block.id == focus.id
    )

    private fun highlight(
        mode: EditorMode,
        block: Block,
        focus: Focus,
        content: Content.Text,
        indent: Int
    ): BlockView.Text.Highlight = BlockView.Text.Highlight(
        mode = if (mode == EditorMode.EDITING) BlockView.Mode.EDIT else BlockView.Mode.READ,
        id = block.id,
        isFocused = block.id == focus.id,
        text = content.text,
        marks = content.marks(),
        indent = indent,
        color = content.color,
        backgroundColor = content.backgroundColor,
        cursor = if (block.id == focus.id) setCursor(focus, content) else null
    )

    private fun toggle(
        mode: EditorMode,
        block: Block,
        content: Content.Text,
        indent: Int,
        focus: Focus,
        isEmpty: Boolean
    ): BlockView.Text.Toggle = BlockView.Text.Toggle(
        mode = if (mode == EditorMode.EDITING) BlockView.Mode.EDIT else BlockView.Mode.READ,
        id = block.id,
        text = content.text,
        marks = content.marks(),
        color = content.color,
        backgroundColor = content.backgroundColor,
        indent = indent,
        isFocused = block.id == focus.id,
        toggled = toggleStateHolder.isToggled(block.id),
        isEmpty = isEmpty,
        cursor = if (block.id == focus.id) setCursor(focus, content) else null
    )

    private fun numbered(
        mode: EditorMode,
        block: Block,
        content: Content.Text,
        number: Int,
        focus: Focus,
        indent: Int
    ): BlockView.Text.Numbered = BlockView.Text.Numbered(
        mode = if (mode == EditorMode.EDITING) BlockView.Mode.EDIT else BlockView.Mode.READ,
        id = block.id,
        text = content.text,
        number = number,
        isFocused = block.id == focus.id,
        color = content.color,
        backgroundColor = content.backgroundColor,
        indent = indent,
        marks = content.marks(),
        cursor = if (block.id == focus.id) setCursor(focus, content) else null
    )

    private fun bookmark(
        mode: EditorMode,
        content: Content.Bookmark,
        block: Block,
        indent: Int
    ): BlockView = content.url?.let { url ->
        if (content.title != null && content.description != null) {
            BlockView.Media.Bookmark(
                id = block.id,
                url = url,
                title = content.title,
                description = content.description,
                imageUrl = content.image?.let { urlBuilder.image(it) },
                faviconUrl = content.favicon?.let { urlBuilder.image(it) },
                indent = indent,
                mode = if (mode == EditorMode.EDITING) BlockView.Mode.EDIT else BlockView.Mode.READ
            )
        } else {
            BlockView.Error.Bookmark(
                id = block.id,
                url = url,
                indent = indent,
                mode = if (mode == EditorMode.EDITING) BlockView.Mode.EDIT else BlockView.Mode.READ
            )
        }
    } ?: BlockView.MediaPlaceholder.Bookmark(
        id = block.id,
        indent = indent,
        mode = if (mode == EditorMode.EDITING) BlockView.Mode.EDIT else BlockView.Mode.READ
    )

    private fun divider(
        block: Block,
        indent: Int
    ) = BlockView.Divider(
        id = block.id,
        indent = indent
    )

    private fun file(
        mode: EditorMode,
        content: Content.File,
        block: Block,
        indent: Int
    ): BlockView = when (content.type) {
        Content.File.Type.IMAGE -> content.toPictureView(
            id = block.id,
            urlBuilder = urlBuilder,
            indent = indent,
            mode = if (mode == EditorMode.EDITING) BlockView.Mode.EDIT else BlockView.Mode.READ
        )
        Content.File.Type.FILE -> content.toFileView(
            id = block.id,
            urlBuilder = urlBuilder,
            indent = indent,
            mode = if (mode == EditorMode.EDITING) BlockView.Mode.EDIT else BlockView.Mode.READ
        )
        Content.File.Type.VIDEO -> content.toVideoView(
            id = block.id,
            urlBuilder = urlBuilder,
            indent = indent,
            mode = if (mode == EditorMode.EDITING) BlockView.Mode.EDIT else BlockView.Mode.READ
        )
        else -> throw IllegalStateException("Unexpected file type: ${content.type}")
    }

    private fun title(
        mode: EditorMode,
        block: Block,
        content: Content.Text,
        root: Block,
        focus: Focus
    ): BlockView.Title.Document = BlockView.Title.Document(
        mode = if (mode == EditorMode.EDITING) BlockView.Mode.EDIT else BlockView.Mode.READ,
        id = block.id,
        text = content.text,
        emoji = root.fields.iconEmoji?.let { name ->
            if (name.isNotEmpty())
                name
            else
                null
        },
        isFocused = block.id == focus.id
    )

    private fun page(
        block: Block,
        content: Content.Link,
        indent: Int,
        details: Block.Details
    ): BlockView.Page = BlockView.Page(
        id = block.id,
        isEmpty = true,
        emoji = details.details[content.target]?.iconEmoji?.let { name ->
            if (name.isNotEmpty())
                name
            else
                null
        },
        image = details.details[content.target]?.iconImage?.let { name ->
            if (name.isNotEmpty())
                urlBuilder.image(name)
            else
                null
        },
        text = details.details[content.target]?.name,
        indent = indent
    )

    private fun setCursor(
        focus: Focus,
        content: Content.Text
    ) : Int? = focus.cursor?.let { cursor ->
        when (cursor) {
            is Cursor.Start -> 0
            is Cursor.End -> content.text.length
            is Cursor.Range -> cursor.range.first
        }
    }
}