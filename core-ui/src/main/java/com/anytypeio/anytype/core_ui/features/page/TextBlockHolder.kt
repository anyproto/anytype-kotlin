package com.anytypeio.anytype.core_ui.features.page

import android.text.Editable
import android.widget.TextView
import com.anytypeio.anytype.core_ui.common.*
import com.anytypeio.anytype.core_ui.extensions.cursorYBottomCoordinate
import com.anytypeio.anytype.core_ui.extensions.preserveSelection
import com.anytypeio.anytype.core_ui.extensions.range
import com.anytypeio.anytype.core_ui.features.editor.holders.`interface`.TextHolder
import com.anytypeio.anytype.core_ui.menu.EditorContextMenu
import com.anytypeio.anytype.core_ui.tools.DefaultSpannableFactory
import com.anytypeio.anytype.core_ui.tools.DefaultTextWatcher
import com.anytypeio.anytype.core_ui.tools.MentionTextWatcher
import com.anytypeio.anytype.core_ui.widgets.text.MentionSpan
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import timber.log.Timber

/**
 * Provides contract and default implementation for text blocks' common behavior.
 * @see [BlockView.Text]
 */
interface TextBlockHolder : TextHolder {

    fun setup(
        onContextMenuStyleClick: (IntRange) -> Unit
    ) {
        with(content) {
            setSpannableFactory(DefaultSpannableFactory())
            setupSelectionActionMode(onContextMenuStyleClick)
        }
    }

    fun setBlockText(
        text: String,
        markup: Markup,
        clicked: (ListenerType) -> Unit,
        textColor: Int
    ) =
        when (markup.marks.isEmpty()) {
            true -> content.setText(text)
            false -> setBlockSpannableText(markup, clicked, textColor)
        }

    private fun setBlockSpannableText(
        markup: Markup,
        clicked: (ListenerType) -> Unit,
        textColor: Int
    ) =
        when (markup.marks.any { it.type == Markup.Type.MENTION }) {
            true -> setSpannableWithMention(markup, clicked, textColor)
            false -> setSpannable(markup, textColor)
        }

    private fun setSpannable(markup: Markup, textColor: Int) {
        content.setText(markup.toSpannable(textColor = textColor), TextView.BufferType.SPANNABLE)
    }

    fun getMentionImageSizeAndPadding(): Pair<Int, Int>

    private fun setSpannableWithMention(markup: Markup,
                                        clicked: (ListenerType) -> Unit,
                                        textColor: Int
    ) {
        content.dismissMentionWatchers()
        content.movementMethod = BetterLinkMovementMethod.getInstance()
        with(content) {
            val sizes = getMentionImageSizeAndPadding()
            setText(
                markup.toSpannable(
                    textColor = textColor,
                    context = context,
                    mentionImageSize = sizes.first,
                    mentionImagePadding = sizes.second,
                    click = { clicked(ListenerType.Mention(it)) },
                    onImageReady = { param -> refreshMentionSpan(param) }
                ),
                TextView.BufferType.SPANNABLE
            )
        }
    }

    fun setBackgroundColor(color: String? = null) {
        Timber.d("Setting background color: $color")
        if (color != null) {
            val value = ThemeColor.values().find { value -> value.title == color }
            if (value != null)
                root.setBackgroundColor(value.background)
            else
                Timber.e("Could not find value for background color: $color")
        } else {
            root.background = null
        }
    }

    fun setMarkup(markup: Markup, clicked: (ListenerType) -> Unit, textColor: Int) {
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
                click = { clicked(ListenerType.Mention(it)) },
                onImageReady = { param -> refreshMentionSpan(param) },
                textColor = textColor
            )
        }

    }

    fun refreshMentionSpan(param: String) {
        content.text?.let { editable ->
            val spans = editable.getSpans(
                0,
                editable.length,
                MentionSpan::class.java
            )
            spans.forEach { span ->
                if (span.param == param) {
                    editable.setSpan(
                        span,
                        editable.getSpanStart(span),
                        editable.getSpanEnd(span),
                        Markup.MENTION_SPANNABLE_FLAG
                    )
                }
            }
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
                setBlockText(
                    text = item.text,
                    markup = item,
                    clicked = clicked,
                    textColor = item.getBlockTextColor()
                )
            }
        } else if (payload.markupChanged()) {
            setMarkup(item, clicked, item.getBlockTextColor())
        }

        if (payload.textColorChanged()) {
            val color = item.color ?: ThemeColor.DEFAULT.title
            setTextColor(color)
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
        onContextMenuStyleClick: (IntRange) -> Unit
    ) {
        with(content) {
            customSelectionActionModeCallback = EditorContextMenu(
                onStyleClick = {
                    preserveSelection {
                        content.hideKeyboard()
                        onContextMenuStyleClick.invoke(content.range())
                        //todo maybe add mode.finish
                    }
                }
            )
        }
    }

    fun clearTextWatchers() {
        content.clearTextWatchers()
    }
}