package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.presentation.editor.editor.KeyPressedEvent
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import kotlinx.android.synthetic.main.item_block_description.view.*
import timber.log.Timber

class Description(view: View) : BlockViewHolder(view) {

    val content: TextInputWidget = itemView.tvBlockDescription

    fun bind(
        view: BlockView.Description
    ) {
        if (view.mode == BlockView.Mode.READ) {
            enableReadMode()
            content.pauseTextWatchers {
                setContent(view)
            }
        } else {
            enableEditMode()
            content.pauseTextWatchers {
                setContent(view)
            }
            setCursor(view)
            setFocus(view)
        }
    }

    fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.Description
    ) {
        payloads.forEach { payload ->

            if (payload.textChanged()) {
                content.pauseSelectionWatcher {
                    content.pauseTextWatchers {
                        setContent(item)
                    }
                }
            }

            if (payload.readWriteModeChanged()) {
                if (item.mode == BlockView.Mode.EDIT) {
                    content.pauseTextWatchers {
                        enableEditMode()
                    }
                } else {
                    enableReadMode()
                }
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
    }

    private fun setCursor(item: BlockView.Description) {
        if (item.isFocused) {
            Timber.d("Setting cursor: $item")
            item.cursor?.let {
                val length = content.text?.length ?: 0
                if (it in 0..length) {
                    content.setSelection(it)
                }
            }
        }
    }

    private fun setFocus(item: BlockView.Description) {
        if (item.isFocused) {
            content.setFocus()
        } else {
            content.clearFocus()
        }
    }

    fun setContent(item: BlockView.Description) {
        content.setText(item.text, TextView.BufferType.EDITABLE)
    }

    fun enableReadMode() {
        itemView.tvBlockDescription.enableReadMode()
        itemView.tvBlockDescription.selectionWatcher = null
        itemView.tvBlockDescription.clearTextWatchers()
    }

    fun enableEditMode() {
        itemView.tvBlockDescription.enableEditMode()
    }

    fun onDescriptionEnterKeyListener(
        views: List<BlockView>,
        textView: TextView,
        range: IntRange,
        onKeyPressedEvent: (KeyPressedEvent) -> Unit
    ) {
        val pos = bindingAdapterPosition
        val text = textView.text.toString()
        if (pos != RecyclerView.NO_POSITION) {
            val view = views[pos]
            check(view is BlockView.Title)
            onKeyPressedEvent.invoke(
                KeyPressedEvent.OnDescriptionBlockEnterKeyEvent(
                    target = view.id,
                    text = text,
                    range = range
                )
            )
        }
    }
}