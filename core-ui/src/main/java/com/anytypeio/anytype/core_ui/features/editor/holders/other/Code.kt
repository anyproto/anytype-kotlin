package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.Focusable
import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.core_ui.features.page.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.page.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.page.ListenerType
import com.anytypeio.anytype.core_ui.tools.DefaultTextWatcher
import com.anytypeio.anytype.core_ui.widgets.text.CodeTextInputWidget
import com.anytypeio.anytype.core_ui.widgets.text.EditorLongClickListener
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.imm
import kotlinx.android.synthetic.main.item_block_code_snippet.view.*
import timber.log.Timber

class Code(view: View) : BlockViewHolder(view) {

    val root: View
        get() = itemView
    val content: CodeTextInputWidget
        get() = itemView.snippet

    fun bind(
        item: BlockView.Code,
        onTextChanged: (String, Editable) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
        clicked: (ListenerType) -> Unit,
        onTextInputClicked: (String) -> Unit
    ) {
        indentize(item)
        if (item.mode == BlockView.Mode.READ) {
            content.setText(item.text)
            content.enableReadMode()
            select(item)
        } else {
            content.enableEditMode()

            select(item)

            content.setOnLongClickListener(
                EditorLongClickListener(
                    t = item.id,
                    click = {
                        content.enableReadMode()
                        onBlockLongClick(root, it, clicked)
                    }
                )
            )

            content.clearTextWatchers()

            content.setText(item.text)

            setFocus(item)

            content.addTextChangedListener(
                DefaultTextWatcher { text ->
                    onTextChanged(item.id, text)
                }
            )

            content.setOnFocusChangeListener { _, focused ->
                item.isFocused = focused
                onFocusChanged(item.id, focused)
            }
            content.selectionWatcher = { onSelectionChanged(item.id, it) }
        }

        content.setOnClickListener { onTextInputClicked(item.id) }
    }

    fun indentize(item: BlockView.Indentable) {
        itemView.snippetContainer.updateLayoutParams<FrameLayout.LayoutParams> {
            apply {
                val extra = item.indent * dimen(R.dimen.indent)
                leftMargin = 0 + extra
            }
        }
    }

    fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.Code,
        onTextChanged: (String, Editable) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
    ) = payloads.forEach { payload ->

        Timber.d("Processing $payload for new view:\n$item")

        if (payload.textChanged()) {
            content.pauseTextWatchers { content.setText(item.text) }
        }

        if (payload.readWriteModeChanged()) {
            if (item.mode == BlockView.Mode.EDIT) {
                content.apply {
                    clearTextWatchers()
                    addTextChangedListener(
                        DefaultTextWatcher { text -> onTextChanged(item.id, text) }
                    )
                    selectionWatcher = { onSelectionChanged(item.id, it) }
                }
                content.enableEditMode()
            } else {
                content.enableReadMode()
            }
        }

        if (payload.selectionChanged()) {
            select(item)
        }

        if (payload.focusChanged()) {
            setFocus(item)
        }

        if (payload.isIndentChanged) {
            indentize(item)
        }
    }

    fun select(item: BlockView.Selectable) {
        root.isSelected = item.isSelected
    }

    fun setFocus(item: Focusable) {
        if (item.isFocused) {
            focus()
        } else {
            content.clearFocus()
        }
    }

    fun focus() {
        Timber.d("Requesting focus")
        content.apply {
            post {
                if (!hasFocus()) {
                    if (requestFocus()) {
                        context.imm().showSoftInput(this, InputMethodManager.SHOW_FORCED)
                    } else {
                        Timber.d("Couldn't gain focus")
                    }
                } else {
                    Timber.d("Already had focus")
                }
            }
        }
    }
}