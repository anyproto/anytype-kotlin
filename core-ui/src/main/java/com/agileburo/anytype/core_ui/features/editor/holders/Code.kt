package com.agileburo.anytype.core_ui.features.editor.holders

import android.text.Editable
import android.view.View
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.agileburo.anytype.core_ui.features.page.ListenerType
import com.agileburo.anytype.core_ui.features.page.TextHolder
import com.agileburo.anytype.core_ui.widgets.text.EditorLongClickListener
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import kotlinx.android.synthetic.main.item_block_code_snippet.view.*

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
            enableReadOnlyMode()
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

            setupTextWatcher(onTextChanged, item)

            content.setOnFocusChangeListener { _, focused ->
                item.isFocused = focused
                onFocusChanged(item.id, focused)
            }
            content.selectionWatcher = { onSelectionChanged(item.id, it) }
        }
    }

    /**
     * Mention is not used in Code
     */
    override fun getMentionImageSizeAndPadding(): Pair<Int, Int> = Pair(0, 0)

    override fun select(item: BlockView.Selectable) {
        root.isSelected = item.isSelected
    }
}