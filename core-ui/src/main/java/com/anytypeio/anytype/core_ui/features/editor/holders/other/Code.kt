package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.text.Editable
import android.view.View
import com.anytypeio.anytype.core_ui.features.editor.holders.`interface`.TextHolder
import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.core_ui.features.page.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.page.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.page.ListenerType
import com.anytypeio.anytype.core_ui.tools.DefaultTextWatcher
import com.anytypeio.anytype.core_ui.widgets.text.EditorLongClickListener
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import kotlinx.android.synthetic.main.item_block_code_snippet.view.*
import timber.log.Timber

class Code(view: View) : BlockViewHolder(view), TextHolder {

    override val root: View
        get() = itemView
    override val content: TextInputWidget
        get() = itemView.snippet

    fun bind(
        item: BlockView.Code,
        onTextChanged: (String, Editable) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
        clicked: (ListenerType) -> Unit
    ) {
        if (item.mode == BlockView.Mode.READ) {
            content.setText(item.text)
            enableReadMode()
            select(item)
        } else {
            enableEditMode()

            select(item)

            content.setOnLongClickListener(
                EditorLongClickListener(
                    t = item.id,
                    click = { onBlockLongClick(root, it, clicked) }
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
    }

    override fun select(item: BlockView.Selectable) {
        root.isSelected = item.isSelected
    }
}