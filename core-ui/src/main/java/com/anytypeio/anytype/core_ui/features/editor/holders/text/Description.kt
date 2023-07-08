package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.view.Gravity
import android.widget.TextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockDescriptionBinding
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.KeyPressedEvent
import com.anytypeio.anytype.presentation.editor.editor.model.Alignment
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import timber.log.Timber

class Description(val binding: ItemBlockDescriptionBinding) : BlockViewHolder(binding.root) {

    val content: TextInputWidget = binding.tvBlockDescription

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
        setupContentPadding(view.isTodoLayout)
        setAlignment(view.alignment)
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

            if (payload.alignmentChanged()) {
                setAlignment(item.alignment)
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
        binding.tvBlockDescription.enableReadMode()
        binding.tvBlockDescription.selectionWatcher = null
    }

    fun enableEditMode() {
        binding.tvBlockDescription.enableEditMode()
    }

    private fun setupContentPadding(isTodoLayout: Boolean) {
        if (isTodoLayout) {
            val lr = itemView.context.dimen(R.dimen.dp_40).toInt()
            binding.tvBlockDescription.setPadding(lr, 0, 0, 0)
        } else {
            binding.tvBlockDescription.setPadding(0, 0, 0, 0)
        }
    }

    private fun setAlignment(alignment: Alignment?) {
        content.gravity = when (alignment) {
            Alignment.CENTER -> Gravity.CENTER
            Alignment.END -> Gravity.END
            else -> Gravity.START
        }
    }
}