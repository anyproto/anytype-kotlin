package com.anytypeio.anytype.core_ui.tools

import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.TextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.Markup
import com.anytypeio.anytype.core_ui.features.page.MentionEvent
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import timber.log.Timber

@Deprecated("legacy")
class BlockTextWatcher : TextWatcher {

    private var locked: Boolean = false
    private var listener: ((Editable) -> Unit)? = null

    fun setListener(listener: (Editable) -> Unit) {
        this.listener = listener
    }

    fun removeListener() {
        this.listener = null
    }

    override fun afterTextChanged(s: Editable) {
        Timber.d("OnTextChanged: $s, locked:$locked, listener:$listener")
        if (!locked)
            listener?.invoke(s)
        else
            Timber.d("Locked text watcher. Skipping text update...")
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    fun lock() {
        locked = true
    }

    fun unlock() {
        locked = false
    }
}

@Deprecated("legacy")
class BlockTextMentionWatcher : TextWatcher {

    private var listener: ((MentionTextWatcherState) -> Unit)? = null

    fun setListener(listener: (MentionTextWatcherState) -> Unit) {
        this.listener = listener
    }

    fun removeListener() {
        this.listener = null
    }

    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    fun onDismiss() {}

    sealed class MentionTextWatcherState {
        data class Start(val start: Int) : MentionTextWatcherState()
        object Stop : MentionTextWatcherState()
        data class Text(val text: CharSequence) : MentionTextWatcherState()
    }
}

class BlockTextBackspaceWatcher : View.OnKeyListener {

    private var listener: (() -> Unit)? = null

    fun setListener(listener: () -> Unit) {
        this.listener = listener
    }

    fun removeListener() {
        this.listener = null
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
            (v as? TextView)?.let {
                if (it.selectionStart == 0 && it.selectionEnd == 0) {
                    listener?.invoke()
                }
            }
        }
        return false
    }
}

class BlockTextEnterWatcher : TextView.OnEditorActionListener {

    sealed class EnterEvent {
        data class Split(val selectionEnd: Int, val text: CharSequence) : EnterEvent()
        data class EndLine(val text: CharSequence) : EnterEvent()
    }

    private var listener: ((EnterEvent) -> Unit)? = null

    fun setListener(listener: (EnterEvent) -> Unit) {
        this.listener = listener
    }

    fun removeListener() {
        this.listener = null
    }

    override fun onEditorAction(view: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (view != null && actionId == TextInputWidget.TEXT_INPUT_WIDGET_ACTION_GO) {
            view.text?.let { text ->
                if (view.selectionEnd < view.text.length) {
                    listener?.invoke(EnterEvent.Split(view.selectionEnd, text))
                } else {
                    listener?.invoke(EnterEvent.EndLine(text))
                }
            }
            return true
        }
        return false
    }
}

class BlockTextMenu(private val menuType: ContextMenuType) : ActionMode.Callback2() {

    private var listener: ((Markup.Type, ActionMode) -> Unit)? = null

    fun setListener(listener: (Markup.Type, ActionMode) -> Unit) {
        this.listener = listener
    }

    fun removeListener() {
        this.listener = null
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(getMenu(menuType), menu)
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.itemBold -> {
                listener?.invoke(Markup.Type.BOLD, mode)
                mode.finish()
                true
            }
            R.id.itemItalic -> {
                listener?.invoke(Markup.Type.ITALIC, mode)
                mode.finish()
                true
            }
            R.id.itemStrike -> {
                listener?.invoke(Markup.Type.STRIKETHROUGH, mode)
                mode.finish()
                true
            }
            R.id.itemCode -> {
                listener?.invoke(Markup.Type.KEYBOARD, mode)
                mode.finish()
                true
            }
            R.id.itemColor -> {
                listener?.invoke(Markup.Type.TEXT_COLOR, mode)
                true
            }
            R.id.itemLink -> {
                listener?.invoke(Markup.Type.LINK, mode)
                mode.finish()
                true
            }
            R.id.itemBackground -> {
                listener?.invoke(Markup.Type.BACKGROUND_COLOR, mode)
                true
            }
            else -> false
        }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {}

    private fun getMenu(type: ContextMenuType) = when (type) {
        ContextMenuType.TEXT -> R.menu.menu_text_style
        ContextMenuType.HEADER -> R.menu.menu_header_style
        ContextMenuType.HIGHLIGHT -> R.menu.menu_highlight_style
    }
}

enum class ContextMenuType { TEXT, HEADER, HIGHLIGHT }
