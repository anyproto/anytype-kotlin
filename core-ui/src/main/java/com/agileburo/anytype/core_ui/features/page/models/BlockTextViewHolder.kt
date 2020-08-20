package com.agileburo.anytype.core_ui.features.page.models

import android.text.Editable
import android.text.SpannableString
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.Alignment
import com.agileburo.anytype.core_ui.common.Focusable
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.common.ThemeColor
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.extensions.cursorYBottomCoordinate
import com.agileburo.anytype.core_ui.extensions.preserveSelection
import com.agileburo.anytype.core_ui.extensions.range
import com.agileburo.anytype.core_ui.features.page.*
import com.agileburo.anytype.core_ui.features.page.BlockTextEvent.KeyboardEvent
import com.agileburo.anytype.core_ui.features.page.BlockTextEvent.MarkupEvent
import com.agileburo.anytype.core_ui.tools.*
import com.agileburo.anytype.core_ui.widgets.text.EditorLongClickListener
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.PopupExtensions
import com.agileburo.anytype.core_utils.ext.hideKeyboard
import com.agileburo.anytype.core_utils.ext.imm
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import timber.log.Timber

abstract class BlockTextViewHolder(
    view: View,
    private val textWatcher: BlockTextWatcher,
    private val mentionWatcher: BlockTextMentionWatcher,
    private val backspaceWatcher: BlockTextBackspaceWatcher,
    private val enterWatcher: BlockTextEnterWatcher,
    private val actionMenu: BlockTextMenu
) : RecyclerView.ViewHolder(view) {

    private val root = itemView
    abstract val content: TextInputWidget
    abstract val spannableFactory: DefaultSpannableFactory

    init {
        with(content) {
            setSpannableFactory(spannableFactory)
            addTextChangedListener(textWatcher)
            addTextChangedListener(mentionWatcher)
            setOnKeyListener(backspaceWatcher)
            setOnEditorActionListener(enterWatcher)
            customSelectionActionModeCallback = actionMenu
        }
    }

    fun bind(
        id: String,
        mode: BlockView.Mode,
        indent: Int,
        text: String? = null,
        textColor: String? = null,
        backgroundColor: String? = null,
        alignment: Alignment? = null,
        selected: Boolean,
        focused: Boolean,
        markup: Markup,
        cursor: BlockView.Cursor,
        click: (ListenerType) -> Unit,
        event: (BlockTextEvent) -> Unit
    ) = when (mode) {
        BlockView.Mode.READ ->
            bindReadMode(
                id = id,
                indent = indent,
                text = text,
                markup = markup,
                textColor = textColor,
                alignment = alignment,
                selected = selected,
                backgroundColor = backgroundColor,
                clicked = click,
                event = event
            )
        BlockView.Mode.EDIT ->
            bindEditMode(
                id = id,
                indent = indent,
                text = text,
                markup = markup,
                textColor = textColor,
                alignment = alignment,
                selected = selected,
                backgroundColor = backgroundColor,
                clicked = click,
                event = event,
                focused = focused,
                cursor = cursor
            )
    }

    private fun bindReadMode(
        id: String,
        indent: Int,
        text: String? = null,
        textColor: String? = null,
        backgroundColor: String? = null,
        alignment: Alignment? = null,
        selected: Boolean,
        markup: Markup,
        clicked: (ListenerType) -> Unit,
        event: (BlockTextEvent) -> Unit
    ) {
        removeListeners()
        enableReadMode()
        indentize(indent)
        setText(text, markup, clicked)
        setTextColor(textColor)
        setBackgroundColor(backgroundColor)
        setAlignment(alignment)
        setSelection(selected)
        setClicks(id, clicked)
        setSelectionListener(id, event)
    }

    private fun bindEditMode(
        id: String,
        indent: Int,
        text: String? = null,
        markup: Markup,
        textColor: String? = null,
        backgroundColor: String? = null,
        alignment: Alignment? = null,
        selected: Boolean,
        focused: Boolean,
        cursor: BlockView.Cursor,
        clicked: (ListenerType) -> Unit,
        event: (BlockTextEvent) -> Unit
    ) {
        enableEditMode()
        indentize(indent)
        setText(text, markup, clicked)
        setTextColor(textColor)
        setBackgroundColor(backgroundColor)
        setAlignment(alignment)
        setSelection(selected)
        setFocus(focused)
        setCursor(cursor)
        setClicks(id, clicked)
        setListeners(id, event)
    }

    // -------------------- MODE ------------------------------
    private fun enableReadMode() {
        content.enableReadMode()
        content.selectionWatcher = null
    }

    private fun enableEditMode() {
        content.enableEditMode()
    }

    // ------------ INDENT ----------------
    abstract fun indentize(indent: Int)

    // ------------ SET TEXT ----------------
    private fun setText(text: String?, markup: Markup?, clicked: (ListenerType) -> Unit) {
        if (text == null) {
            content.text = null
        } else {
            if (markup == null || markup.marks.isNullOrEmpty()) {
                content.setText(text)
            } else {
                setBlockSpannableText(markup, clicked)
            }
        }
    }

    private fun setBlockSpannableText(markup: Markup, clicked: (ListenerType) -> Unit) {
        if (markup.marks.any { it.type == Markup.Type.MENTION }) {
            setSpannableWithMention(markup, clicked)
        } else {
            setSpannable(markup)
        }
    }

    private fun setSpannable(markup: Markup) {
        content.setText(getSpannableText(markup), TextView.BufferType.SPANNABLE)
    }

    private fun setSpannableWithMention(markup: Markup, clicked: (ListenerType) -> Unit) =
        with(content) {
            movementMethod = BetterLinkMovementMethod.getInstance()
            setText(buildSpannableTextWithMention(markup, clicked), TextView.BufferType.SPANNABLE)
        }

    private fun getSpannableText(markup: Markup): SpannableString =
        SpannableString(markup.body).apply {
            setMarkup(markup = markup)
        }

    private fun buildSpannableTextWithMention(
        markup: Markup,
        clicked: ((ListenerType) -> Unit)? = null
    ): SpannableString {
        val sizes = getMentionSizes()
        return SpannableString(markup.body).apply {
            setMarkup(
                markup = markup,
                context = content.context,
                mentionImageSize = sizes.first,
                mentionImagePadding = sizes.second,
                click = {
                    clicked?.invoke(ListenerType.Mention(it))
                }
            )
        }
    }

    // ------------ EDITABLE UPDATE MARKUP ----------------
    private fun updateEditableMarkup(
        editable: Editable,
        markup: Markup?,
        clicked: (ListenerType) -> Unit
    ) {
        if ((markup == null || markup.marks.isNullOrEmpty())) {
            editable.clearSpans()
        } else {
            updateMarkup(editable, markup, clicked)
        }
    }

    abstract fun getMentionSizes(): Pair<Int, Int>

    private fun updateMarkup(editable: Editable, markup: Markup, clicked: (ListenerType) -> Unit) {
        if (markup.marks.any { it.type == Markup.Type.MENTION }) {
            val sizes = getMentionSizes()
            content.dismissMentionWatchers()
            content.movementMethod = BetterLinkMovementMethod.getInstance()
            editable.setMarkup(
                markup = markup,
                context = content.context,
                mentionImageSize = sizes.first,
                mentionImagePadding = sizes.second,
                click = {
                    clicked.invoke(ListenerType.Mention(it))
                }
            )
        } else {
            editable.setMarkup(markup)
        }
    }

    // ------------ TEXT COLOR, BACKGROUND COLOR ----------------
    private fun setTextColor(textColor: String?) {
        if (textColor != null)
            setTextColor(textColor)
        else
            content.setTextColor(content.context.color(R.color.black))
    }

    private fun setBackgroundColor(color: String?) {
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

    // ------------ SELECTION ----------------
    private fun setSelection(selected: Boolean) {
        content.isSelected = selected
    }

    // ------------ FOCUS ----------------
    private fun setFocus(focused: Boolean) {
        if (focused) {
            content.apply {
                post {
                    if (!hasFocus()) {
                        if (requestFocus()) {
                            context.imm().showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                        } else {
                            Timber.d("Couldn't gain focus")
                        }
                    } else
                        Timber.d("Already had focus")
                }
            }
        } else {
            content.clearFocus()
        }
    }

    // ------------ CURSOR ----------------
    private fun setCursor(item: BlockView.Cursor) {
        item.cursor?.let {
            val length = content.text?.length ?: 0
            if (it in 0..length) {
                content.setSelection(it)
            }
        }
    }

    // ------------ ALIGNMENT ----------------
    private fun setAlignment(alignment: Alignment? = null) {
        if (alignment != null) {
            content.gravity = when (alignment) {
                Alignment.START -> Gravity.START
                Alignment.CENTER -> Gravity.CENTER
                Alignment.END -> Gravity.END
            }
        }
    }

    // ------------ CLICKS ----------------
    private fun setClicks(id: String, clicked: (ListenerType) -> Unit) {
        content.setOnLongClickListener(
            EditorLongClickListener(
                t = id,
                click = { onBlockLongClick(root, it, clicked) }
            )
        )
        content.setOnClickListener {
            clicked(ListenerType.EditableBlock(id))
        }
    }

    private fun onBlockLongClick(root: View, target: String, clicked: (ListenerType) -> Unit) {
        val rect = PopupExtensions.calculateRectInWindow(root)
        val dimensions = BlockDimensions(
            left = rect.left,
            top = rect.top,
            bottom = rect.bottom,
            right = rect.right,
            height = root.height,
            width = root.width
        )
        clicked(ListenerType.LongClick(target, dimensions))
    }

    // ------------ LISTENERS ----------------
    private fun setListeners(
        id: String,
        event: (BlockTextEvent) -> Unit
    ) {
        setTextListener(id, event)
        setMentionListener(event)
        setBackspaceListener(id, event)
        setEnterListener(id, event)
        setClipboardListener(event)
        setSelectionListener(id, event)
        setFocusListener(id, event)
        setActionModeListener(event)
    }

    private fun removeListeners() {
        removeTextListener()
        removeMentionListener()
        removeBackspaceListener()
        removeEnterListener()
        removeClipboardListener()
        removeSelectionListener()
        removeFocusListener()
        removeActionModeListener()
    }

    // ------------ TEXT LISTENER ----------------
    private fun setTextListener(
        id: String,
        event: (BlockTextEvent) -> Unit
    ) {
        textWatcher.setListener { editable -> onTextEvent(event, id, editable) }
    }

    private fun removeTextListener() {
        textWatcher.removeListener()
    }

    abstract fun onTextEvent(event: (BlockTextEvent) -> Unit, id: String, editable: Editable)

    // ------------ MENTION LISTENER ----------------
    private fun setMentionListener(event: (BlockTextEvent) -> Unit) {
        mentionWatcher.setListener { state ->
            when (state) {
                is BlockTextMentionWatcher.MentionTextWatcherState.Start -> {
                    event(
                        BlockTextEvent.MentionEvent.Start(
                            cursorCoordinate = content.cursorYBottomCoordinate(),
                            mentionStart = state.start
                        )
                    )
                }
                BlockTextMentionWatcher.MentionTextWatcherState.Stop -> {
                    event.invoke(BlockTextEvent.MentionEvent.Stop)
                }
                is BlockTextMentionWatcher.MentionTextWatcherState.Text -> {
                    event.invoke(BlockTextEvent.MentionEvent.Text(state.text))
                }
            }
        }
    }

    private fun removeMentionListener() {
        mentionWatcher.removeListener()
        mentionWatcher.onDismiss()
    }

    // ------------ ENTER LISTENER ----------------
    private fun setEnterListener(
        id: String,
        event: (BlockTextEvent) -> Unit
    ) {
        enterWatcher.setListener { enterEvent ->
            when (enterEvent) {
                is BlockTextEnterWatcher.EnterEvent.Split ->
                    event(
                        KeyboardEvent.SplitLineEnter(
                            target = id,
                            index = enterEvent.selectionEnd,
                            text = enterEvent.text
                        )
                    )
                is BlockTextEnterWatcher.EnterEvent.EndLine ->
                    event(KeyboardEvent.EndLineEnter(target = id, text = content.editableText))
            }
        }
    }

    private fun removeEnterListener() {
        enterWatcher.removeListener()
    }

    // ------------ BACKSPACE LISTENER ----------------
    private fun setBackspaceListener(
        id: String,
        event: (BlockTextEvent) -> Unit
    ) {
        backspaceWatcher.setListener {
            if (content.text?.isEmpty() == true) {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    event(KeyboardEvent.EmptyBlockBackspace(id))
                } else {
                    Timber.e("Holder.adapter position is -1")
                }
            } else {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    content.text?.let { editable ->
                        event(KeyboardEvent.NonEmptyBlockBackspace(id, editable))
                    }
                } else {
                    Timber.e("Holder.adapter position is -1")
                }
            }
        }
    }

    private fun removeBackspaceListener() {
        backspaceWatcher.removeListener()
    }

    // ------------ SELECTION LISTENER ----------------
    private fun setSelectionListener(id: String, event: (BlockTextEvent) -> Unit) {
        content.selectionWatcher =
            { range -> event(BlockTextEvent.SelectionEvent(id, range)) }
    }

    private fun removeSelectionListener() {
        content.selectionWatcher = null
    }

    // ------------ FOCUS LISTENER ----------------
    private fun setFocusListener(id: String, event: (BlockTextEvent) -> Unit) {
        content.setOnFocusChangeListener { _, focus ->
            event(BlockTextEvent.FocusEvent(id, focus))
        }
    }

    private fun removeFocusListener() {
        content.onFocusChangeListener = null
    }

    // ------------ CLIPBOARD LISTENER ----------------
    private fun setClipboardListener(event: (BlockTextEvent) -> Unit) {
        content.clipboardInterceptor = object : ClipboardInterceptor {
            override fun onClipboardAction(action: ClipboardInterceptor.Action) {
                when (action) {
                    is ClipboardInterceptor.Action.Copy ->
                        event.invoke(BlockTextEvent.Action.Copy(action.selection))
                    is ClipboardInterceptor.Action.Paste ->
                        event.invoke(BlockTextEvent.Action.Paste(action.selection))
                }
            }
        }
    }

    private fun removeClipboardListener() {
        content.clipboardInterceptor = null
    }

    // ------------ ACTION MODE LISTENER ----------------
    private fun setActionModeListener(event: (BlockTextEvent) -> Unit) {
        actionMenu.setListener { type, mode ->
            when (type) {
                Markup.Type.TEXT_COLOR -> {
                    content.preserveSelection {
                        content.hideKeyboard()
                        event.invoke(MarkupEvent(Markup.Type.TEXT_COLOR, content.range()))
                        mode.finish()
                    }
                }
                Markup.Type.BACKGROUND_COLOR -> {
                    content.preserveSelection {
                        content.hideKeyboard()
                        event.invoke(MarkupEvent(Markup.Type.BACKGROUND_COLOR, content.range()))
                        mode.finish()
                    }
                }
                else -> {
                    event.invoke(MarkupEvent(type, content.range()))
                }
            }
        }
    }

    private fun removeActionModeListener() {
        actionMenu.removeListener()
    }

    // ------------ PAYLOADS ----------------
    private fun payloadText(
        payload: BlockViewDiffUtil.Payload,
        item: BlockView.Text,
        clicked: (ListenerType) -> Unit
    ) {
        if (payload.isTextChanged) {
            val markup = item as? Markup
            content.pauseTextWatchers {
                setText(text = item.text, clicked = clicked, markup = markup)
            }
        }
        if (payload.isMarkupChanged) {
            content.text?.let { editable ->
                val markup = item as? Markup
                updateEditableMarkup(editable = editable, markup = markup, clicked = clicked)
            }
        }
        if (payload.isTextColorChanged) {
            setTextColor(item.color)
        }
        if (payload.isBackgroundColorChanged) {
            setBackgroundColor(item.backgroundColor)
        }
        if (payload.isAlignmentChanged) {
            val alignment = item as? Alignment
            setAlignment(alignment)
        }
        if (payload.isCursorChanged) {
            (item as? BlockView.Cursor)?.let {
                setCursor(it)
            }
        }
    }

    private fun payloadPermission(
        payload: BlockViewDiffUtil.Payload,
        item: BlockView.Permission
    ) {
        if (payload.isModeChanged) {
            when (item.mode) {
                BlockView.Mode.READ -> enableReadMode()
                BlockView.Mode.EDIT -> enableEditMode()
            }
        }
    }

    private fun payloadSelectable(
        payload: BlockViewDiffUtil.Payload,
        item: BlockView.Selectable
    ) {
        if (payload.isSelectionChanged) {
            setSelection(item.isSelected)
        }
    }

    private fun payloadFocusable(
        payload: BlockViewDiffUtil.Payload,
        item: Focusable
    ) {
        if (payload.isFocusChanged) {
            setSelection(item.isFocused)
        }
    }

    fun payload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView,
        clicked: (ListenerType) -> Unit
    ) = payloads.forEach { payload ->
        when (item) {
            is BlockView.Text -> payloadText(payload, item, clicked)
            is BlockView.Permission -> payloadPermission(payload, item)
            is BlockView.Selectable -> payloadSelectable(payload, item)
            is Focusable -> payloadFocusable(payload, item)
        }
    }
}