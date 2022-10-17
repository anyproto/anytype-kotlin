package com.anytypeio.anytype.core_ui.features.table

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTableRowItemEditableBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTableRowItemEmptyBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTableSpaceBinding
import com.anytypeio.anytype.core_ui.features.editor.ItemProviderAdapter
import com.anytypeio.anytype.core_ui.features.editor.marks
import com.anytypeio.anytype.core_ui.features.editor.withBlock
import com.anytypeio.anytype.core_ui.features.table.holders.TableCellHolder
import com.anytypeio.anytype.core_ui.features.table.holders.EditableCellHolder
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionEvent
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent

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
        return (items[pos] as? BlockView.Table.Cell.Text)?.block
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
                            val item = items[pos]
                            if (item is BlockView.Table.Cell.Text) {
                                clicked(
                                    ListenerType.TableTextCell(
                                        tableId = tableBlockId,
                                        cellId = item.block.id
                                    )
                                )
                            }
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
                            val item = items[pos]
                            if (item is BlockView.Table.Cell.Text) {
                                item.block.cursor = selection.last
                                onSelectionChanged(item.block.id, selection)
                            }
                        }
                    }
                    content.setOnFocusChangeListener { _, hasFocus ->
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            val item = items[pos]
                            if (item is BlockView.Table.Cell.Text) {
                                cellSelection(hasFocus)
                                onFocusChanged(item.block.id, hasFocus)
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
                            val item = items[bindingAdapterPosition]
                            if (item is BlockView.Table.Cell.Empty) {
                                clicked(
                                    ListenerType.TableEmptyCell(
                                        cellId = item.getId(),
                                        rowId = item.rowId,
                                        tableId = tableBlockId
                                    )
                                )
                            }
                        }
                    }
                }
            }
            TYPE_SPACE -> {
                val binding = ItemBlockTableSpaceBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return TableCellHolder.TableSpaceHolder(binding)
            }
            else -> throw UnsupportedOperationException("wrong viewtype:$viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is EditableCellHolder) {
            holder.bind(
                item = (items[position] as BlockView.Table.Cell.Text).block
            )
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

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is BlockView.Table.Cell.Empty -> TYPE_EMPTY
        is BlockView.Table.Cell.Text -> TYPE_CELL
        BlockView.Table.Cell.Space -> TYPE_SPACE
    }

    companion object {
        const val TYPE_CELL = 1
        const val TYPE_SPACE = 2
        const val TYPE_EMPTY = 3
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
            if (newItem is BlockView.Table.Cell.Space && oldItem is BlockView.Table.Cell.Space) {
                return true
            }
            if (newItem is BlockView.Table.Cell.Empty && oldItem is BlockView.Table.Cell.Empty) {
                return newItem.rowId == oldItem.rowId && newItem.columnId == oldItem.columnId
            }
            if (newItem is BlockView.Table.Cell.Text && oldItem is BlockView.Table.Cell.Text) {
                return newItem.rowId == oldItem.rowId && newItem.columnId == oldItem.columnId
            }
            return false
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return old[oldItemPosition] == new[newItemPosition]
        }
    }
}