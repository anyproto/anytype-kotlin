package com.anytypeio.anytype.presentation.page.editor.styling

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.presentation.page.editor.Markup
import com.anytypeio.anytype.presentation.page.editor.model.Alignment

data class StyleConfig(
    val visibleTypes: List<StylingType>,
    val enabledMarkup: List<Markup.Type>,
    val enabledAlignment: List<Alignment>
) {

    companion object {
        fun emptyState() = StyleConfig(
            visibleTypes = emptyList(),
            enabledMarkup = emptyList(),
            enabledAlignment = emptyList()
        )
    }
}

fun Block.getStyleConfig(focus: Boolean?, selection: IntRange?): StyleConfig =
    when (val cnt = content) {
        is Block.Content.Text -> cnt.getTextStyleConfig(focus, selection)
        is Block.Content.Link -> cnt.getLinkStyleConfig()
        is Block.Content.File -> cnt.getFileStyleConfig(focus)
        is Block.Content.Bookmark -> cnt.getBookmarkStyleConfig(focus)
        is Block.Content.RelationBlock -> cnt.getRelationStyleConfig()
        else -> StyleConfig.emptyState()
    }

fun Block.Content.Bookmark.getBookmarkStyleConfig(focus: Boolean?): StyleConfig {
    check(focus == null) { "Bookmark block should has null focus" }
    return getStyleConfig()
}

fun Block.Content.Bookmark.getStyleConfig(): StyleConfig = StyleConfig.emptyState()

fun Block.Content.Link.getLinkStyleConfig(): StyleConfig = StyleConfig(
    visibleTypes = listOf(StylingType.BACKGROUND),
    enabledMarkup = emptyList(),
    enabledAlignment = emptyList()
)

fun Block.Content.RelationBlock.getRelationStyleConfig(): StyleConfig = StyleConfig(
    visibleTypes = listOf(StylingType.BACKGROUND),
    enabledMarkup = emptyList(),
    enabledAlignment = emptyList()
)

fun Block.Content.File.getFileStyleConfig(focus: Boolean?): StyleConfig {
    check(focus == null) { "File block should has null focus" }
    return getStyleConfig()
}

fun Block.Content.File.getStyleConfig(): StyleConfig = when (type) {
    Block.Content.File.Type.FILE -> {
        StyleConfig(
            visibleTypes = listOf(StylingType.BACKGROUND),
            enabledMarkup = emptyList(),
            enabledAlignment = emptyList()
        )
    }
    Block.Content.File.Type.IMAGE -> {
        StyleConfig(
            visibleTypes = listOf(StylingType.BACKGROUND),
            enabledMarkup = emptyList(),
            enabledAlignment = emptyList()
        )
    }
    Block.Content.File.Type.VIDEO -> {
        StyleConfig(
            visibleTypes = listOf(StylingType.BACKGROUND),
            enabledMarkup = emptyList(),
            enabledAlignment = emptyList()
        )
    }
    else -> StyleConfig.emptyState()
}

fun Block.Content.Text.getTextStyleConfig(focus: Boolean?, selection: IntRange?): StyleConfig {
    return if (focus != null && focus) {
        getStyleConfig(selection = selection)
    } else {
        getStyleConfig(selection = null)
    }
}

fun Block.Content.Text.getStyleConfig(selection: IntRange? = null): StyleConfig {
    return if (selection == null || selection.first >= selection.last) {
        getBlockStyle(style)
    } else {
        getMarkupStyle(style)
    }
}

fun Block.Content.Text.getBlockStyle(style: Block.Content.Text.Style) = when (style) {
    Block.Content.Text.Style.P -> {
        StyleConfig(
            visibleTypes = listOf(
                StylingType.STYLE,
                StylingType.TEXT_COLOR,
                StylingType.BACKGROUND
            ),
            enabledMarkup = listOf(
                Markup.Type.BOLD,
                Markup.Type.ITALIC,
                Markup.Type.STRIKETHROUGH,
                Markup.Type.KEYBOARD,
                Markup.Type.LINK
            ),
            enabledAlignment = listOf(Alignment.START, Alignment.CENTER, Alignment.END)
        )
    }
    Block.Content.Text.Style.H1, Block.Content.Text.Style.H2,
    Block.Content.Text.Style.H3, Block.Content.Text.Style.H4 -> {
        StyleConfig(
            visibleTypes = listOf(
                StylingType.STYLE,
                StylingType.TEXT_COLOR,
                StylingType.BACKGROUND
            ),
            enabledMarkup = listOf(
                Markup.Type.ITALIC,
                Markup.Type.STRIKETHROUGH,
                Markup.Type.KEYBOARD,
                Markup.Type.LINK
            ),
            enabledAlignment = listOf(Alignment.START, Alignment.CENTER, Alignment.END)
        )
    }
    Block.Content.Text.Style.TITLE -> {
        StyleConfig(
            visibleTypes = listOf(StylingType.BACKGROUND),
            enabledMarkup = emptyList(),
            enabledAlignment = listOf(Alignment.START, Alignment.CENTER, Alignment.END)
        )
    }
    Block.Content.Text.Style.QUOTE -> {
        StyleConfig(
            visibleTypes = listOf(
                StylingType.STYLE,
                StylingType.TEXT_COLOR,
                StylingType.BACKGROUND
            ),
            enabledMarkup = listOf(
                Markup.Type.BOLD,
                Markup.Type.ITALIC,
                Markup.Type.STRIKETHROUGH,
                Markup.Type.KEYBOARD,
                Markup.Type.LINK
            ),
            enabledAlignment = listOf(Alignment.START, Alignment.END)
        )
    }
    Block.Content.Text.Style.CODE_SNIPPET -> {
        StyleConfig(
            visibleTypes = listOf(StylingType.BACKGROUND),
            enabledMarkup = emptyList(),
            enabledAlignment = emptyList()
        )
    }
    Block.Content.Text.Style.BULLET, Block.Content.Text.Style.NUMBERED,
    Block.Content.Text.Style.TOGGLE, Block.Content.Text.Style.CHECKBOX -> {
        StyleConfig(
            visibleTypes = listOf(
                StylingType.STYLE,
                StylingType.TEXT_COLOR,
                StylingType.BACKGROUND
            ),
            enabledMarkup = listOf(
                Markup.Type.BOLD,
                Markup.Type.ITALIC,
                Markup.Type.STRIKETHROUGH,
                Markup.Type.KEYBOARD,
                Markup.Type.LINK
            ),
            enabledAlignment = emptyList()
        )
    }
}

fun Block.Content.Text.getMarkupStyle(style: Block.Content.Text.Style) = when (style) {
    Block.Content.Text.Style.P -> {
        StyleConfig(
            visibleTypes = listOf(
                StylingType.STYLE,
                StylingType.TEXT_COLOR,
                StylingType.BACKGROUND
            ),
            enabledMarkup = listOf(
                Markup.Type.BOLD,
                Markup.Type.ITALIC,
                Markup.Type.STRIKETHROUGH,
                Markup.Type.KEYBOARD,
                Markup.Type.LINK
            ),
            enabledAlignment = emptyList()
        )
    }
    Block.Content.Text.Style.H1, Block.Content.Text.Style.H2,
    Block.Content.Text.Style.H3, Block.Content.Text.Style.H4 -> {
        StyleConfig(
            visibleTypes = listOf(
                StylingType.STYLE,
                StylingType.TEXT_COLOR,
                StylingType.BACKGROUND
            ),
            enabledMarkup = listOf(
                Markup.Type.ITALIC,
                Markup.Type.STRIKETHROUGH,
                Markup.Type.KEYBOARD,
                Markup.Type.LINK
            ),
            enabledAlignment = emptyList()
        )
    }
    Block.Content.Text.Style.TITLE -> {
        StyleConfig(
            visibleTypes = listOf(StylingType.BACKGROUND),
            enabledMarkup = emptyList(),
            enabledAlignment = emptyList()
        )
    }
    Block.Content.Text.Style.QUOTE -> {
        StyleConfig(
            visibleTypes = listOf(
                StylingType.STYLE,
                StylingType.TEXT_COLOR,
                StylingType.BACKGROUND
            ),
            enabledMarkup = listOf(
                Markup.Type.BOLD,
                Markup.Type.ITALIC,
                Markup.Type.STRIKETHROUGH,
                Markup.Type.KEYBOARD,
                Markup.Type.LINK
            ),
            enabledAlignment = emptyList()
        )
    }
    Block.Content.Text.Style.CODE_SNIPPET -> {
        StyleConfig(
            visibleTypes = emptyList(),
            enabledMarkup = emptyList(),
            enabledAlignment = emptyList()
        )
    }
    Block.Content.Text.Style.BULLET, Block.Content.Text.Style.NUMBERED,
    Block.Content.Text.Style.TOGGLE, Block.Content.Text.Style.CHECKBOX -> {
        StyleConfig(
            visibleTypes = listOf(
                StylingType.STYLE,
                StylingType.TEXT_COLOR,
                StylingType.BACKGROUND
            ),
            enabledMarkup = listOf(
                Markup.Type.BOLD,
                Markup.Type.ITALIC,
                Markup.Type.STRIKETHROUGH,
                Markup.Type.KEYBOARD,
                Markup.Type.LINK
            ),
            enabledAlignment = emptyList()
        )
    }
}

