package com.anytypeio.anytype.core_ui.features.table

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTableCellBinding
import com.anytypeio.anytype.core_ui.features.editor.ItemProviderAdapter
import com.anytypeio.anytype.core_ui.features.table.holders.EditableCellHolder
import com.anytypeio.anytype.core_ui.tools.ClipboardInterceptor
import com.anytypeio.anytype.core_ui.tools.LockableFocusChangeListener
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionEvent
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import timber.log.Timber

class TableEditableCellsAdapter(
    private var items: List<BlockView.Table.Cell>,
    private val clicked: (ListenerType) -> Unit,
    private val onTextBlockTextChanged: (BlockView.Text, String, List<Markup.Mark>) -> Unit,
    private val onMentionEvent: (MentionEvent) -> Unit,
    private val onCellSelectionChanged: (Id, IntRange) -> Unit,
    private val onFocusChanged: (Id, Boolean) -> Unit,
    private val clipboardInterceptor: ClipboardInterceptor,
    private val onDragAndDropTrigger: (RecyclerView.ViewHolder, event: MotionEvent?) -> Boolean
) : RecyclerView.Adapter<EditableCellHolder>(),
    ItemProviderAdapter<BlockView.Text.Paragraph?> {

    override fun provide(pos: Int): BlockView.Text.Paragraph? {
        return items.getOrNull(pos)?.block
    }

    fun updateWithDiffUtil(items: List<BlockView.Table.Cell>) {
        val result = DiffUtil.calculateDiff(TableCellsDiffUtil(old = this.items, new = items))
        this.items = items
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditableCellHolder {
        val binding = ItemBlockTableCellBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EditableCellHolder(
            binding = binding,
            clicked = clicked
        ).apply {
            content.editorTouchProcessor.onLongClick = {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    clicked(ListenerType.CellLongClick(tableId = items[pos].tableId))
                }
            }
            content.editorTouchProcessor.onDragAndDropTrigger = {
                onDragAndDropTrigger(this, it)
            }
            content.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val cell = items[pos]
                    if (cell.block != null) {
                        clicked(ListenerType.TableTextCell(cell))
                    } else {
                        clicked(ListenerType.TableEmptyCell(cell))
                    }
                }
            }
            content.selectionWatcher = { selection ->
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val block = items[pos].block
                    if (block != null) {
                        block.cursor = selection.last
                        onCellSelectionChanged(block.id, selection)
                    }
                }
            }
            content.onFocusChangeListener = LockableFocusChangeListener { hasFocus ->
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val block = items[pos].block
                    if (block != null) {
                        block.isFocused = hasFocus
                        cellSelection(hasFocus)
                        onFocusChanged(block.id, hasFocus)
                    }
                }
            }
            content.clipboardInterceptor = clipboardInterceptor
            content.expandCellHitArea(root)
            this.setupViewHolder(
                onTextChanged = { editable ->
//                    this.withBlock<BlockView.Text> { item ->
//                        item.apply {
//                            text = editable.toString()
//                            marks = editable.marks()
//                        }
//                        onTextBlockTextChanged(item)
//                    }
                },
                onEmptyBlockBackspaceClicked = {},
                onSplitLineEnterClicked = { _, _, _ -> },
                onNonEmptyBlockBackspaceClicked = { _, _ -> },
                onMentionEvent = { mentionEvent ->
                    onMentionEvent(
                        when (mentionEvent) {
                            MentionEvent.MentionSuggestStop -> MentionEvent.MentionSuggestStopCell
                            else -> mentionEvent
                        }
                    )
                },
                onSlashEvent = {},
                onBackPressedCallback = null
            )
        }
    }

    override fun onBindViewHolder(holder: EditableCellHolder, position: Int) {
        val block = items[position].block
        if (block != null) {
            holder.bind(block)
        } else {
            holder.bindEmptyCell()
        }
    }

    override fun onBindViewHolder(
        holder: EditableCellHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        Timber.d("Update Table cells with payload, position:[$position], payloads:[$payloads]")
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val block = items[position].block
            if (block != null) {
                holder.processChangePayload(
                    payloads = payloads.typeOf(),
                    item = block,
                    clicked = clicked
                )
            }
        }
    }

    override fun getItemCount(): Int = items.size

}

fun View.expandCellHitArea(parent: View) {
    parent.post {
        val parentRect = Rect()
        val childRect = Rect()
        parent.getHitRect(parentRect)
        this.getHitRect(childRect)
        childRect.left = 0
        childRect.top = 0
        childRect.right = parentRect.width()
        childRect.bottom = 2000
        parent.touchDelegate = TouchDelegate(childRect, this)
    }
}