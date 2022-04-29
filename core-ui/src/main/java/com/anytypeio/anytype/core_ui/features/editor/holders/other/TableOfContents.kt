package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.ViewCompat.generateViewId
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTocBinding
import com.anytypeio.anytype.core_ui.extensions.lighter
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil.Companion.BACKGROUND_COLOR_CHANGED
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil.Companion.SELECTION_CHANGED
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportCustomTouchProcessor
import com.anytypeio.anytype.core_ui.widgets.text.TableOfContentsItemWidget
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import timber.log.Timber

class TableOfContents(
    val binding: ItemBlockTocBinding
) : BlockViewHolder(binding.root),
    BlockViewHolder.DragAndDropHolder,
    SupportCustomTouchProcessor {

    val root: FrameLayout = binding.root
    val container: LinearLayout = binding.container
    private val selected = binding.selected
    private val defPadding = root.resources.getDimension(R.dimen.def_toc_item_padding_start).toInt()

    override val editorTouchProcessor = EditorTouchProcessor(
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(item: BlockView.TableOfContents, clicked: (ListenerType) -> Unit) {
        cleanup()
        selected.isSelected = item.isSelected
        item.items.forEach { header ->
            val textview = TableOfContentsItemWidget(root.context).apply {
                id = generateViewId()
                setPadding(getPadding(header.depth), 0, 0, 0)
                setName(header.name)
                setOnClickListener {
                    clicked.invoke(
                        ListenerType.TableOfContentsItem(
                            item = header.id,
                            target = item.id
                        )
                    )
                }
                setOnLongClickListener {
                    clicked.invoke(ListenerType.LongClick(item.id))
                    true
                }
            }
            container.addView(textview)
        }
        applyBackground(item.backgroundColor)
    }

    private fun applyBackground(
        background: String?
    ) {
        Timber.d("Setting background color: $background")
        if (!background.isNullOrEmpty()) {
            val value = ThemeColor.values().find { value -> value.title == background }
            if (value != null && value != ThemeColor.DEFAULT) {
                root.setBackgroundColor(itemView.resources.lighter(value, 0))
            } else {
                Timber.e("Could not find value for background color: $background, setting background to null")
                root.setBackgroundColor(0)
            }
        } else {
            Timber.d("Background color is null, setting background to null")
            container.setBackgroundColor(0)
        }
    }

    private fun getPadding(depth: Int): Int {
        return if (depth >= MAX_DEPTH) {
            MAX_DEPTH * defPadding
        } else {
            depth * defPadding
        }
    }

    private fun cleanup() {
        if (container.childCount > 0) {
            container.removeAllViews()
        }
    }

    fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.TableOfContents
    ) {
        payloads.forEach { payload ->
            if (payload.changes.contains(SELECTION_CHANGED)) {
                selected.isSelected = item.isSelected
            }
            if (payload.changes.contains(BACKGROUND_COLOR_CHANGED)) {
                applyBackground(item.backgroundColor)
            }
        }
    }


    companion object {
        const val MAX_DEPTH = 2
    }
}