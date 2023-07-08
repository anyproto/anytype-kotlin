package com.anytypeio.anytype.core_ui.tools

import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.features.editor.holders.other.Title
import com.anytypeio.anytype.core_ui.features.editor.holders.relations.FeaturedRelationListViewHolder
import kotlin.properties.Delegates

/**
 * @property [threshold] threshold space between header and top toolbar with sync status when header and toolbar are considered overlaid.
 * @property [thresholdPadding] extra space for [threshold]
 */
class EditorHeaderOverlayDetector(
    private val threshold: Int,
    private val thresholdPadding: Int,
    onHeaderOverlayStateChanged: (Boolean) -> Unit
) : RecyclerView.OnScrollListener() {

    private var onHeaderOverlaid: Boolean by Delegates.observable(true) { _, old, new ->
        if (new != old) onHeaderOverlayStateChanged(new)
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (recyclerView.layoutManager is LinearLayoutManager) {
            val lm = (recyclerView.layoutManager as LinearLayoutManager)
            val isFirstItemVisible = lm.findFirstVisibleItemPosition() == 0
            if (isFirstItemVisible) {
                when (val holder = recyclerView.findViewHolderForLayoutPosition(0)) {
                    is Title.Document -> {
                        val root = holder.binding.root
                        val title = holder.binding.title
                        val cover = holder.binding.cover
                        val emojiIconContainer = holder.binding.docEmojiIconContainer
                        val imageIconContainer = holder.binding.imageIcon
                        onHeaderOverlaid = when {
                            emojiIconContainer.isVisible -> {
                                if (cover.isVisible) {
                                    (emojiIconContainer.top + root.top >= threshold + thresholdPadding)
                                } else {
                                    (emojiIconContainer.top + root.top >= (threshold / 2) + thresholdPadding)
                                }
                            }
                            imageIconContainer.isVisible -> {
                                if (cover.isVisible) {
                                    (imageIconContainer.top + root.top >= threshold + thresholdPadding)
                                } else {
                                    (imageIconContainer.top + root.top >= (threshold / 2) + thresholdPadding)
                                }
                            }
                            else -> {
                                root.top + title.top >= threshold + thresholdPadding
                            }
                        }
                    }
                    is Title.Todo -> {
                        val root = holder.binding.root
                        val title = holder.binding.title
                        onHeaderOverlaid = if (holder.binding.cover.isVisible) {
                            root.top + title.bottom >= threshold + thresholdPadding
                        } else {
                            root.top + title.top >= threshold + thresholdPadding
                        }
                    }
                    is Title.Profile -> {
                        val container = holder.itemView
                        val cover = holder.binding.cover
                        val icon = holder.binding.docProfileIconContainer
                        val title = holder.binding.title
                        onHeaderOverlaid = if (cover.isVisible) {
                            (container.top + icon.bottom >= threshold + thresholdPadding)
                        } else {
                            (container.top + title.top >= (threshold / 2) + thresholdPadding)
                        }
                    }
                    is FeaturedRelationListViewHolder -> {
                        // Handling note layout when header and title are missing.
                        onHeaderOverlaid = holder.itemView.top >= threshold
                    }
                }
            } else {
                onHeaderOverlaid = false
            }
        }
    }
}