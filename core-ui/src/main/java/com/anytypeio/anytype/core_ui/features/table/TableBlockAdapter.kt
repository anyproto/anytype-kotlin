package com.anytypeio.anytype.core_ui.features.table

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTableRowItemBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTableSpaceBinding
import com.anytypeio.anytype.core_ui.features.table.holders.TableCellHolder
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class TableBlockAdapter(
    differ: TableCellsDiffUtil,
    private val clickListener: (ListenerType) -> Unit
) : ListAdapter<BlockView.Table.Cell, TableCellHolder>(differ) {

    private var tableBlockId = ""

    fun setTableBlockId(id: Id) {
        tableBlockId = id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableCellHolder {
        when (viewType) {
            TYPE_CELL -> {
                val binding = ItemBlockTableRowItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return TableCellHolder.TableTextCellHolder(
                    context = parent.context,
                    binding = binding
                ).apply {
                    textContent.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onCellClicked(getItem(pos))
                        }
                    }
                    textContent.setOnLongClickListener { _ ->
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            clickListener(ListenerType.LongClick(tableBlockId))
                        }
                        true
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

    private fun onCellClicked(item: BlockView.Table.Cell) {
        when (item) {
            is BlockView.Table.Cell.Empty -> clickListener(
                ListenerType.TableEmptyCell(
                    cellId = item.getId(),
                    rowId = item.rowId,
                    tableId = tableBlockId
                )
            )
            is BlockView.Table.Cell.Text ->
                clickListener(
                    ListenerType.TableTextCell(
                        tableId = tableBlockId,
                        cellId = item.block.id
                    )
                )
            BlockView.Table.Cell.Space -> {}
        }
    }

    override fun onBindViewHolder(holder: TableCellHolder, position: Int) {
        if (holder is TableCellHolder.TableTextCellHolder) {
            holder.bind(getItem(position))
        }
    }

    override fun onBindViewHolder(
        holder: TableCellHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            if (holder is TableCellHolder.TableTextCellHolder) {
                holder.processChangePayload(
                    payloads = payloads.typeOf<TableCellsDiffUtil.Payload>().first(),
                    cell = getItem(position)
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is BlockView.Table.Cell.Empty -> TYPE_CELL
        is BlockView.Table.Cell.Text -> TYPE_CELL
        BlockView.Table.Cell.Space -> TYPE_SPACE
    }

    companion object {
        const val TYPE_CELL = 1
        const val TYPE_SPACE = 2
    }
}