package com.anytypeio.anytype.core_ui.features.editor.holders.media

import android.annotation.SuppressLint
import android.view.View
import com.anytypeio.anytype.core_ui.extensions.lighter
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportCustomTouchProcessor
import com.anytypeio.anytype.core_utils.ext.PopupExtensions
import com.anytypeio.anytype.presentation.editor.editor.BlockDimensions
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import timber.log.Timber

@SuppressLint("ClickableViewAccessibility")
abstract class Media(view: View) : BlockViewHolder(view),
    BlockViewHolder.IndentableHolder,
    BlockViewHolder.DragAndDropHolder,
    SupportCustomTouchProcessor {

    abstract val root: View
    abstract val clickContainer: View
    abstract val container: View
    abstract fun onMediaBlockClicked(item: BlockView.Media, clicked: (ListenerType) -> Unit)
    abstract override fun indentize(item: BlockView.Indentable)
    abstract fun select(isSelected: Boolean)

    override val editorTouchProcessor = EditorTouchProcessor(
        fallback = { e -> clickContainer.onTouchEvent(e) }
    )

    fun bind(
        item: BlockView.Media,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)
        select(item.isSelected)
        with(clickContainer) {
            setOnClickListener { onMediaBlockClicked(item, clicked) }
        }
    }

    open fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.Media) { "Expected a media, but was: $item" }
        payloads.forEach { payload ->
            if (payload.isSelectionChanged) {
                select(item.isSelected)
            }
        }
    }

    private fun mediaLongClick(root: View, target: String, clicked: (ListenerType) -> Unit) {
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

    protected fun applyBackground(
        background: String?
    ) {
        Timber.d("Setting background color: $background")
        if (!background.isNullOrEmpty()) {
            val value = ThemeColor.values().find { value -> value.title == background }
            if (value != null && value != ThemeColor.DEFAULT) {
                container.setBackgroundColor(itemView.resources.lighter(value, 0))
            } else {
                Timber.e("Could not find value for background color: $background, setting background to null")
                container.setBackgroundColor(0)
            }
        } else {
            Timber.d("Background color is null, setting background to null")
            container.setBackgroundColor(0)
        }
    }
}