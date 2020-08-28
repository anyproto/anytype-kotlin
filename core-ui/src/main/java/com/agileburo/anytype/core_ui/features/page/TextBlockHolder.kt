package com.agileburo.anytype.core_ui.features.page

import android.text.Editable
import android.widget.TextView
import com.agileburo.anytype.core_ui.common.*
import com.agileburo.anytype.core_ui.extensions.cursorYBottomCoordinate
import com.agileburo.anytype.core_ui.extensions.preserveSelection
import com.agileburo.anytype.core_ui.extensions.range
import com.agileburo.anytype.core_ui.features.editor.holders.`interface`.TextHolder
import com.agileburo.anytype.core_ui.menu.ContextMenuType
import com.agileburo.anytype.core_ui.menu.TextBlockContextMenu
import com.agileburo.anytype.core_ui.tools.DefaultSpannableFactory
import com.agileburo.anytype.core_ui.tools.DefaultTextWatcher
import com.agileburo.anytype.core_ui.tools.MentionTextWatcher
import com.agileburo.anytype.core_utils.ext.hideKeyboard
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import timber.log.Timber

/**
 * Provides contract and default implementation for text blocks' common behavior.
 * @see [BlockView.Text]
 */
interface TextBlockHolder : TextHolder {

    fun setup(
        onMarkupActionClicked: (Markup.Type, IntRange) -> Unit,
        menuType: ContextMenuType
    ) {
        with(content) {
            setSpannableFactory(DefaultSpannableFactory())
            setupSelectionActionMode(onMarkupActionClicked, menuType)
        }
    }

    fun setBlockText(text: String, markup: Markup, clicked: (ListenerType) -> Unit) =
        when (markup.marks.isEmpty()) {
            true -> content.setText(text)
            false -> setBlockSpannableText(markup, clicked)
        }

    private fun setBlockSpannableText(markup: Markup, clicked: (ListenerType) -> Unit) =
        when (markup.marks.any { it.type == Markup.Type.MENTION }) {
            true -> setSpannableWithMention(markup, clicked)
            false -> setSpannable(markup)
        }

    private fun setSpannable(markup: Markup) {
        content.setText(markup.toSpannable(), TextView.BufferType.SPANNABLE)
    }

    fun getMentionImageSizeAndPadding(): Pair<Int, Int>

    private fun setSpannableWithMention(markup: Markup, clicked: (ListenerType) -> Unit) {
        content.dismissMentionWatchers()
        content.movementMethod = BetterLinkMovementMethod.getInstance()
        with(content) {
            val sizes = getMentionImageSizeAndPadding()
            setText(
                markup.toSpannable(
                    context = context,
                    mentionImageSize = sizes.first,
                    mentionImagePadding = sizes.second,
                    click = { clicked(ListenerType.Mention(it)) }
                ),
                TextView.BufferType.SPANNABLE
            )
        }
    }

    fun setBackgroundColor(color: String? = null) {
        Timber.d("Setting background color: $color")
        if (color != null) {
            root.setBackgroundColor(
                ThemeColor.values().first { value ->
                    value.title == color
                }.background
            )
        } else {
            root.background = null
        }
    }

    fun setMarkup(markup: Markup, clicked: (ListenerType) -> Unit) {
        if (markup.marks.isLinksPresent()) {
            content.setLinksClickable()
        }
        with(content) {
            val sizes = getMentionImageSizeAndPadding()
            text?.setMarkup(
                markup = markup,
                context = context,
                mentionImageSize = sizes.first,
                mentionImagePadding = sizes.second,
                click = { clicked(ListenerType.Mention(it)) })
        }

    }

    fun setupTextWatcher(
        item: BlockView,
        onMentionEvent: ((MentionEvent) -> Unit)? = null,
        onTextChanged: (String, Editable) -> Unit,
    ) {
        content.addTextChangedListener(
            DefaultTextWatcher { text ->
                onTextChanged(item.id, text)
            }
        )
        setupMentionWatcher(onMentionEvent)
    }

    fun setupMentionWatcher(
        onMentionEvent: ((MentionEvent) -> Unit)?
    ) {
        content.addTextChangedListener(
            MentionTextWatcher { state ->
                when (state) {
                    is MentionTextWatcher.MentionTextWatcherState.Start -> {
                        onMentionEvent?.invoke(
                            MentionEvent.MentionSuggestStart(
                                cursorCoordinate = content.cursorYBottomCoordinate(),
                                mentionStart = state.start
                            )
                        )
                    }
                    MentionTextWatcher.MentionTextWatcherState.Stop -> {
                        onMentionEvent?.invoke(MentionEvent.MentionSuggestStop)
                    }

                    is MentionTextWatcher.MentionTextWatcherState.Text -> {
                        onMentionEvent?.invoke(MentionEvent.MentionSuggestText(state.text))
                    }
                }
            }
        )
    }

    fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView,
        onTextChanged: (BlockView.Text) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        clicked: (ListenerType) -> Unit
    ) = payloads.forEach { payload ->

        check(item is BlockView.Text)

        Timber.d("Processing $payload for new view:\n$item")

        if (payload.textChanged()) {
            content.pauseTextWatchers {
                when (item) {
                    is BlockView.Text.Paragraph -> {
                        setBlockText(text = item.text, markup = item, clicked = clicked)
                    }
                    else -> {
                        content.setText(item.toSpannable(), TextView.BufferType.SPANNABLE)
                    }
                }
            }
        } else if (payload.markupChanged()) {
            setMarkup(item, clicked)
        }

        if (payload.textColorChanged()) {
            item.color?.let { setTextColor(it) }
        }

        if (payload.backgroundColorChanged()) {
            setBackgroundColor(item.backgroundColor)
        }

        if (payload.alignmentChanged()) {
            item.alignment?.let { setAlignment(it) }
        }

        if (payload.readWriteModeChanged()) {
            if (item.mode == BlockView.Mode.EDIT) {
                content.clearTextWatchers()
                setupTextWatcher(item) { id, editable ->
                    item.apply {
                        text = editable.toString()
                        marks = editable.marks()
                    }
                    onTextChanged(item)
                }
                content.selectionWatcher = { onSelectionChanged(item.id, it) }
                enableEditMode()
            } else {
                enableReadMode()
            }
        }

        if (payload.selectionChanged()) {
            select(item)
        }

        if (payload.focusChanged()) {
            setFocus(item)
        }

        try {
            if (payload.isCursorChanged) {
                item.cursor?.let {
                    content.setSelection(it)
                }
            }
        } catch (e: Throwable) {
            Timber.e(e, "Error while setting cursor from $item")
        }
    }

    private fun setupSelectionActionMode(
        onMarkupActionClicked: (Markup.Type, IntRange) -> Unit,
        menuType: ContextMenuType
    ) {
        with(content) {
            customSelectionActionModeCallback = TextBlockContextMenu(
                menuType = menuType,
                onTextColorClicked = { mode ->
                    preserveSelection {
                        content.hideKeyboard()
                        onMarkupActionClicked(Markup.Type.TEXT_COLOR, content.range())
                        mode.finish()
                    }
                    false
                },
                onBackgroundColorClicked = { mode ->
                    preserveSelection {
                        content.hideKeyboard()
                        onMarkupActionClicked(Markup.Type.BACKGROUND_COLOR, content.range())
                        mode.finish()
                    }
                    false
                },
                onMenuItemClicked = { onMarkupActionClicked(it, content.range()) }
            )
        }
    }

    fun clearTextWatchers() {
        content.clearTextWatchers()
    }
}