package com.anytypeio.anytype.core_ui.features.editor.holders.placeholders

import android.view.View
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockMediaPlaceholderBinding
import com.anytypeio.anytype.core_ui.extensions.veryLight
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportCustomTouchProcessor
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.google.android.material.card.MaterialCardView
import timber.log.Timber

abstract class MediaPlaceholder(binding: ItemBlockMediaPlaceholderBinding) :
    BlockViewHolder(binding.root),
    BlockViewHolder.DragAndDropHolder,
    BlockViewHolder.IndentableHolder,
    SupportCustomTouchProcessor {

    protected val root: View = binding.root
    protected val card: MaterialCardView = binding.card
    protected val title: TextView = binding.title

    abstract fun placeholderClick(target: String, clicked: (ListenerType) -> Unit)
    abstract fun setup()

    override val editorTouchProcessor = EditorTouchProcessor(
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(
        item: BlockView.MediaPlaceholder,
        clicked: (ListenerType) -> Unit
    ) {
        setup()
        applyRootMargins(item)
        indentize(item)
        select(item.isSelected)
        applyBackground(item.backgroundColor)
        with(itemView) {
            setOnClickListener { placeholderClick(item.id, clicked) }
        }
    }

    fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.MediaPlaceholder) { "Expected a media placeholder, but was: $item" }
        payloads.forEach { payload ->
            if (payload.isSelectionChanged) {
                select(item.isSelected)
            }
            if (payload.isBackgroundColorChanged) {
                applyBackground(item.backgroundColor)
            }
        }
    }

    private fun applyRootMargins(item: BlockView.MediaPlaceholder) {
        if (item.isPreviousBlockMedia) {
            root.updateLayoutParams<RecyclerView.LayoutParams> {
                apply { topMargin = 0 }
            }
        } else {
            root.updateLayoutParams<RecyclerView.LayoutParams> {
                apply {
                    topMargin =
                        root.resources.getDimension(R.dimen.default_media_placeholder_root_margin_top)
                            .toInt()
                }
            }
        }
    }

    private fun select(isSelected: Boolean) {
        root.isSelected = isSelected
    }

    private fun applyBackground(
        background: String?
    ) {
        Timber.d("Setting background color: $background")
        if (!background.isNullOrEmpty()) {
            val value = ThemeColor.values().find { value -> value.code == background }
            if (value != null && value != ThemeColor.DEFAULT) {
                card.setCardBackgroundColor(card.resources.veryLight(value, 0))
            } else {
                Timber.e("Could not find value for background color: $background, setting background to null")
                card.setCardBackgroundColor(0)
            }
        } else {
            Timber.d("Background color is null, setting background to null")
            card.setCardBackgroundColor(0)
        }
    }

    override fun indentize(item: BlockView.Indentable) {
        val leftPadding =
            dimen(R.dimen.default_document_item_padding_start) + item.indent * dimen(R.dimen.indent)
        root.updatePadding(left = leftPadding)
    }
}

