package com.agileburo.anytype.core_ui.features.page.models

import android.text.Editable
import android.text.SpannableString
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.R
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
import com.agileburo.anytype.core_ui.features.page.holders.Holder
import com.agileburo.anytype.core_ui.tools.*
import com.agileburo.anytype.core_ui.widgets.text.EditorLongClickListener
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.PopupExtensions
import com.agileburo.anytype.core_utils.ext.hideKeyboard
import com.agileburo.anytype.core_utils.ext.imm
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import timber.log.Timber


interface Item : Focusable, BlockView.Text, BlockView.Permission, BlockView.Cursor, Markup {
    val id: String
    override val body: String get() = text
}

abstract class BlockTextViewHolder(
    view: View,
    private val textWatcher: BlockTextWatcher,
    private val mentionWatcher: BlockTextMentionWatcher,
    private val backspaceWatcher: BlockTextBackspaceWatcher,
    private val enterWatcher: BlockTextEnterWatcher,
    private val actionMenu: BlockTextMenu
) : RecyclerView.ViewHolder(view), Holder.Selectable, Holder.Indentable {

    val root = itemView

    abstract val content: TextInputWidget

    open val spannableFactory: DefaultSpannableFactory = DefaultSpannableFactory()

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
        click: (ListenerType) -> Unit,
        event: (BlockTextEvent) -> Unit,
        item: Item
    ) = when (item.mode) {
        BlockView.Mode.READ ->
            bindReadMode(
                id = item.id,
                text = item.text,
                markup = item,
                textColor = item.color,
                backgroundColor = item.backgroundColor,
                clicked = click,
                event = event
            )
        BlockView.Mode.EDIT ->
            bindEditMode(
                id = item.id,
                text = item.text,
                markup = item,
                textColor = item.color,
                backgroundColor = item.backgroundColor,
                clicked = click,
                event = event,
                focused = item.isFocused,
                cursor = item,
                item = item
            )
    }

    private fun bindReadMode(
        id: String,
        text: String? = null,
        textColor: String? = null,
        backgroundColor: String? = null,
        markup: Markup,
        clicked: (ListenerType) -> Unit,
        event: (BlockTextEvent) -> Unit
    ) {
        removeListeners()
        enableReadMode()
        setText(text, markup, clicked)
        setTextColor(textColor)
        setBackgroundColor(backgroundColor)
        setClicks(id, clicked)
        setSelectionListener(id, event)
    }

    private fun bindEditMode(
        id: String,
        text: String? = null,
        markup: Markup,
        textColor: String? = null,
        backgroundColor: String? = null,
        focused: Boolean,
        cursor: BlockView.Cursor,
        clicked: (ListenerType) -> Unit,
        event: (BlockTextEvent) -> Unit,
        item: Item
    ) {
        enableEditMode()
        setText(text, markup, clicked)
        setTextColor(textColor)
        setBackgroundColor(backgroundColor)
        setFocus(focused)
        setCursor(cursor)
        setClicks(id, clicked)
        setListeners(id, event, item)
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
    abstract override fun indentize(indent: Int)

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

    open fun setTextColor(textColor: String?) {
        if (textColor != null)
            content.setTextColor(
                ThemeColor.values().first { value ->
                    value.title == textColor
                }.text
            )
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
        event: (BlockTextEvent) -> Unit,
        item: Item
    ) {
        setTextListener(id, event, item)
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
        event: (BlockTextEvent) -> Unit,
        item: Item
    ) {
        textWatcher.setListener { editable ->
            onTextEvent(event, id, item, editable)
        }
    }

    private fun removeTextListener() {
        textWatcher.removeListener()
    }

    abstract fun onTextEvent(event: (BlockTextEvent) -> Unit, id: String, item: Item, editable: Editable)

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
        item: Item,
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

    private fun payloadFocusable(
        payload: BlockViewDiffUtil.Payload,
        item: Focusable
    ) {
        if (payload.isFocusChanged) {
            setFocus(item.isFocused)
        }
    }

    open fun payload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: Item,
        clicked: (ListenerType) -> Unit
    ) = payloads.forEach { payload ->
        payloadText(payload, item, clicked)
        payloadPermission(payload, item)
        payloadFocusable(payload, item)
    }
}