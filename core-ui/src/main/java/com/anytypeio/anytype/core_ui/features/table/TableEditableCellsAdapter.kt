package com.anytypeio.anytype.core_ui.features.table

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTableRowItemEditableBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTableRowItemEmptyBinding
import com.anytypeio.anytype.core_ui.features.editor.ItemProviderAdapter
import com.anytypeio.anytype.core_ui.features.editor.marks
import com.anytypeio.anytype.core_ui.features.editor.withBlock
import com.anytypeio.anytype.core_ui.features.table.holders.EditableCellHolder
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionEvent
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent
import timber.log.Timber

class TableEditableCellsAdapter(
    private var items: List<BlockView.Table.Cell>,
    private val clicked: (ListenerType) -> Unit,
    private val onTextBlockTextChanged: (BlockView.Text) -> Unit,
    private val onMentionEvent: (MentionEvent) -> Unit,
    private val onSlashEvent: (SlashEvent) -> Unit,
    private val onSelectionChanged: (Id, IntRange) -> Unit,
    private val onFocusChanged: (Id, Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    ItemProviderAdapter<BlockView.Text.Paragraph?> {

    private var tableBlockId = ""

    fun setTableBlockId(id: Id) {
        tableBlockId = id
    }

    override fun provide(pos: Int): BlockView.Text.Paragraph? {
        return items.getOrNull(pos)?.block
    }

    fun updateWithDiffUtil(items: List<BlockView.Table.Cell>) {
        val result = DiffUtil.calculateDiff(TableCellsDiffUtil(old = this.items, new = items))
        this.items = items
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_CELL -> {
                val binding = ItemBlockTableRowItemEditableBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return EditableCellHolder(
                    binding = binding,
                    clicked = clicked
                ).apply {
                    content.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            clicked(
                                ListenerType.TableTextCell(cell = items[pos])
                            )
                        }
                    }
                    content.setOnLongClickListener { _ ->
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            clicked(ListenerType.LongClick(tableBlockId))
                        }
                        true
                    }
                    content.selectionWatcher = { selection ->
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            val block = items[pos].block
                            if (block != null) {
                                block.cursor = selection.last
                                onSelectionChanged(block.id, selection)
                            }
                        }
                    }
                    content.setOnFocusChangeListener { _, hasFocus ->
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            val block = items[pos].block
                            if (block != null) {
                                cellSelection(hasFocus)
                                onFocusChanged(block.id, hasFocus)
                            }
                        }
                    }
                    this.setupViewHolder(
                        onTextChanged = { editable ->
                            this.withBlock<BlockView.Text> { item ->
                                item.apply {
                                    text = editable.toString()
                                    marks = editable.marks()
                                }
                                onTextBlockTextChanged(item)
                            }
                        },
                        onEmptyBlockBackspaceClicked = {},
                        onSplitLineEnterClicked = { _, _, _ -> },
                        onNonEmptyBlockBackspaceClicked = { _, _ -> },
                        onMentionEvent = onMentionEvent,
                        onSlashEvent = onSlashEvent,
                        onBackPressedCallback = null
                    )
                }
            }
            TYPE_EMPTY -> {
                val binding = ItemBlockTableRowItemEmptyBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return EmptyHolder(binding = binding).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            clicked(
                                ListenerType.TableEmptyCell(cell = items[pos])
                            )
                        }
                    }
                }
            }
            else -> throw UnsupportedOperationException("wrong viewtype:$viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is EditableCellHolder) {
            val block = items[position].block
            if (block != null) {
                holder.bind(item = block)
            } else {
                Timber.w("onBindViewHolder Cell, block is null")
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return if (items[position].block == null) {
            TYPE_EMPTY
        } else {
            TYPE_CELL
        }
    }

    companion object {
        const val TYPE_CELL = 1
        const val TYPE_EMPTY = 2
    }

    class EmptyHolder(binding: ItemBlockTableRowItemEmptyBinding) :
        RecyclerView.ViewHolder(binding.root)

    class TableCellsDiffUtil(
        private val old: List<BlockView.Table.Cell>,
        private val new: List<BlockView.Table.Cell>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = old.size
        override fun getNewListSize() = new.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val newItem = new[newItemPosition]
            val oldItem = old[oldItemPosition]
            return newItem.rowId == oldItem.rowId && newItem.columnId == oldItem.columnId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return old[oldItemPosition].block == new[newItemPosition].block
        }
    }
}