package com.anytypeio.anytype.core_ui.features.editor

import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.Spannable
import android.widget.TextView
import com.anytypeio.anytype.core_ui.common.*
import com.anytypeio.anytype.core_ui.extensions.applyMovementMethod
import com.anytypeio.anytype.core_ui.extensions.cursorYBottomCoordinate
import com.anytypeio.anytype.core_ui.extensions.preserveSelection
import com.anytypeio.anytype.core_ui.extensions.range
import com.anytypeio.anytype.core_ui.features.editor.holders.`interface`.TextHolder
import com.anytypeio.anytype.core_ui.menu.EditorContextMenu
import com.anytypeio.anytype.core_ui.tools.*
import com.anytypeio.anytype.core_ui.widgets.text.MentionSpan
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import com.anytypeio.anytype.core_utils.ext.removeSpans
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionEvent
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.Checkable
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent
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
            //setupSelectionActionMode(onContextMenuStyleClick)
        }
    }

    fun setBlockText(
        text: String,
        markup: Markup,
        clicked: (ListenerType) -> Unit,
        textColor: Int
    ) {
        content.applyMovementMethod(markup)
        when (markup.marks.isEmpty()) {
            true -> content.setText(text)
            false -> setBlockSpannableText(markup, clicked, textColor)
        }
    }

    private fun setBlockSpannableText(
        markup: Markup,
        clicked: (ListenerType) -> Unit,
        textColor: Int
    ) {
        when (markup.marks.any { it is Markup.Mark.Mention || it is Markup.Mark.Object }) {
            true -> setSpannableWithMention(markup, clicked, textColor)
            false -> setSpannable(markup, textColor)
        }
    }

    private fun setSpannable(markup: Markup, textColor: Int) {
        content.setText(markup.toSpannable(textColor = textColor), TextView.BufferType.SPANNABLE)
    }

    fun getMentionIconSize(): Int
    fun getMentionIconPadding(): Int
    fun getMentionCheckedIcon(): Drawable?
    fun getMentionUncheckedIcon(): Drawable?
    fun getMentionInitialsSize(): Float

    private fun setSpannableWithMention(markup: Markup,
                                        clicked: (ListenerType) -> Unit,
                                        textColor: Int
    ) {
        content.dismissMentionWatchers()
        with(content) {
            setText(
                markup.toSpannable(
                    textColor = textColor,
                    context = context,
                    mentionImageSize = getMentionIconSize(),
                    mentionImagePadding = getMentionIconPadding(),
                    mentionCheckedIcon = getMentionCheckedIcon(),
                    mentionUncheckedIcon = getMentionUncheckedIcon(),
                    click = { clicked(ListenerType.Mention(it)) },
                    onImageReady = { param -> refreshMentionSpan(param) },
                    mentionInitialsSize = getMentionInitialsSize()
                ),
                TextView.BufferType.SPANNABLE
            )
        }
    }

    fun setBackgroundColor(color: String? = null) {
        Timber.d("Setting background color: $color")
        if (!color.isNullOrEmpty()) {
            val value = ThemeColor.values().find { value -> value.title == color }
            if (value != null) {
                root.setBackgroundColor(value.background)
            } else {
                Timber.e("Could not find value for background color: $color, setting background to null")
                root.background = null
            }
        } else {
            Timber.d("Background color is null, setting background to null")
            root.background = null
        }
    }

    fun setMarkup(markup: Markup, clicked: (ListenerType) -> Unit, textColor: Int) {
        content.applyMovementMethod(markup)
        with(content) {
            text?.setMarkup(
                markup = markup,
                context = context,
                mentionImageSize = getMentionIconSize(),
                mentionImagePadding = getMentionIconPadding(),
                click = { clicked(ListenerType.Mention(it)) },
                onImageReady = { param -> refreshMentionSpan(param) },
                textColor = textColor,
                mentionCheckedIcon = getMentionCheckedIcon(),
                mentionUncheckedIcon = getMentionUncheckedIcon(),
                mentionInitialsSize = getMentionInitialsSize()
            )
        }
    }

    fun applyCheckedCheckboxColorSpan(isChecked: Boolean) {
        content.editableText.removeSpans<CheckedCheckboxColorSpan>()
        if (isChecked) {
            content.editableText.setSpan(
                CheckedCheckboxColorSpan(),
                0,
                content.editableText.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
    }

    fun applySearchHighlight(item: BlockView.Searchable) {
        content.editableText.removeSpans<SearchHighlightSpan>()
        content.editableText.removeSpans<SearchTargetHighlightSpan>()
        item.searchFields.forEach { field ->
            field.highlights.forEach { highlight ->
                content.editableText.setSpan(
                    SearchHighlightSpan(),
                    highlight.first,
                    highlight.last,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            if (field.isTargeted) {
                content.editableText.setSpan(
                    SearchTargetHighlightSpan(),
                    field.target.first,
                    field.target.last,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    fun applyGhostEditorSelection(item: BlockView.SupportGhostEditorSelection) {
        content.editableText.removeSpans<GhostEditorSelectionSpan>()
        item.ghostEditorSelection?.let { range ->
            content.editableText.setSpan(
                GhostEditorSelectionSpan(),
                range.first,
                range.last,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
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
        onMentionEvent: ((MentionEvent) -> Unit),
        onSlashEvent: (SlashEvent) -> Unit,
        onTextChanged: (String, Editable) -> Unit,
    ) {
        content.addTextChangedListener(
            DefaultTextWatcher { text ->
                onTextChanged(item.id, text)
            }
        )
        setupMentionWatcher(onMentionEvent)
        setupSlashWatcher(onSlashEvent, item.getViewType())
    }

    fun setupMentionWatcher(
        onMentionEvent: ((MentionEvent) -> Unit)
    ) {
        content.addTextChangedListener(
            MentionTextWatcher { state ->
                when (state) {
                    is MentionTextWatcher.MentionTextWatcherState.Start -> {
                        onMentionEvent.invoke(
                            MentionEvent.MentionSuggestStart(
                                cursorCoordinate = content.cursorYBottomCoordinate(),
                                mentionStart = state.start
                            )
                        )
                    }
                    MentionTextWatcher.MentionTextWatcherState.Stop -> {
                        onMentionEvent.invoke(MentionEvent.MentionSuggestStop)
                    }

                    is MentionTextWatcher.MentionTextWatcherState.Text -> {
                        onMentionEvent.invoke(MentionEvent.MentionSuggestText(state.text))
                    }
                }
            }
        )
    }

    fun setupSlashWatcher(
        onSlashEvent: (SlashEvent) -> Unit,
        viewType: Int
    ) {
        content.addTextChangedListener(
            SlashTextWatcher { state ->
                when (state) {
                    is SlashTextWatcherState.Start -> onSlashEvent(
                        SlashEvent.Start(
                            slashStart = state.start,
                            cursorCoordinate = content.cursorYBottomCoordinate()
                        )
                    )
                    SlashTextWatcherState.Stop -> onSlashEvent(SlashEvent.Stop)
                    is SlashTextWatcherState.Filter -> onSlashEvent(
                        SlashEvent.Filter(
                            filter = state.text,
                            viewType = viewType
                        )
                    )
                }
            }
        )
    }

    fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView,
        onTextChanged: (BlockView.Text) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        clicked: (ListenerType) -> Unit,
        onMentionEvent: (MentionEvent) -> Unit,
        onSlashEvent: (SlashEvent) -> Unit
    ) = payloads.forEach { payload ->

        check(item is BlockView.Text)

        Timber.d("Processing $payload for new view:\n$item")

        if (payload.textChanged()) {
            content.pauseSelectionWatcher {
                content.pauseTextWatchers {
                    setBlockText(
                        text = item.text,
                        markup = item,
                        clicked = clicked,
                        textColor = item.getBlockTextColor()
                    )
                }
            }
            if (item is Checkable) {
                applyCheckedCheckboxColorSpan(item.isChecked)
            }
        } else if (payload.markupChanged()) {
            content.pauseTextWatchers {
                setMarkup(item, clicked, item.getBlockTextColor())
                if (item is Checkable) {
                    applyCheckedCheckboxColorSpan(item.isChecked)
                }
            }
        }

        if (payload.isSearchHighlightChanged) {
            applySearchHighlight(item)
        }

        if (payload.isGhostEditorSelectionChanged) {
            applyGhostEditorSelection(item)
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
            content.pauseTextWatchers {
                if (item.mode == BlockView.Mode.EDIT) {
                    content.clearTextWatchers()
                    setupTextWatcher(
                        item = item,
                        onTextChanged = { _, editable ->
                            item.apply {
                                text = editable.toString()
                                marks = editable.marks()
                            }
                            onTextChanged(item)
                        },
                        onMentionEvent = onMentionEvent,
                        onSlashEvent = onSlashEvent
                    )
                    //content.selectionWatcher = { onSelectionChanged(item.id, it) }
                    content.pauseTextWatchers {
                        enableEditMode()
                    }
                    content.pauseTextWatchers {
                        content.applyMovementMethod(item)
                    }
                } else {
                    enableReadMode()
                }
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