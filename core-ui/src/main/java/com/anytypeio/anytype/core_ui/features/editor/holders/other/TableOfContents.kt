package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.ViewCompat.generateViewId
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTocBinding
import com.anytypeio.anytype.core_ui.extensions.setBlockBackgroundColor
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil.Companion.BACKGROUND_COLOR_CHANGED
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil.Companion.SELECTION_CHANGED
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportCustomTouchProcessor
import com.anytypeio.anytype.core_ui.widgets.text.TableOfContentsItemWidget
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

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

    fun bind(item: BlockView.TableOfContents, clicked: (ListenerType) -> Unit) {
        cleanup()
        selected.isSelected = item.isSelected
        item.items.forEach { header ->
            val textview = TableOfContentsItemWidget(root.context).apply {
                id = generateViewId()
                setPadding(getPadding(header.depth), 0, 0, 0)
                setName(header.name)
                setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
                setOnClickListener {
                    clicked.invoke(
                        ListenerType.TableOfContentsItem(
                            item = header.id,
                            target = item.id
                        )
                    )
                }
            }
            container.addView(textview)
        }
        applyBackground(item.backgroundColor)
    }

    private fun applyBackground(background: String?) {
        root.setBlockBackgroundColor(background)
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