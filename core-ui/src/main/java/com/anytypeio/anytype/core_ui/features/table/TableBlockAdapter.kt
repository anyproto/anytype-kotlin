package com.anytypeio.anytype.core_ui.features.table

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTableRowItemBinding
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

    private fun onCellClicked(item: BlockView.Table.Cell) {
        val block = item.block
        if (block == null) {
            clickListener(
                ListenerType.TableEmptyCell(cell = item)
            )
        } else {
            clickListener(
                ListenerType.TableTextCell(cell = item)
            )
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
}