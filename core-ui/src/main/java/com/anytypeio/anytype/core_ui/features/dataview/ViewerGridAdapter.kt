package com.anytypeio.anytype.core_ui.features.dataview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.sets.model.CellView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import kotlinx.android.synthetic.main.item_viewer_grid_row.view.*
import timber.log.Timber

class ViewerGridAdapter(
    private val onCellClicked: (CellView) -> Unit,
    private val onObjectHeaderClicked: (String, String?) -> Unit
) : ListAdapter<Viewer.GridView.Row, ViewerGridAdapter.RecordHolder>(GridDiffUtil) {

    var recordNamePositionX = 0f

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecordHolder {
        Timber.d("OnCreateViewHolder")
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_viewer_grid_row, parent, false)
        view.rowCellRecycler.apply {
            adapter = ViewerGridCellsAdapter(
                onCellClicked = onCellClicked
            )
        }
        return RecordHolder(view).apply {
            itemView.headerContainer.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val item = getItem(pos)
                    onObjectHeaderClicked(item.id, item.type)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecordHolder, position: Int) {
        Timber.d("Binding record holder")
        holder.bindObjectHeader(getItem(position))
        holder.bindObjectCells(getItem(position))
    }

    override fun onBindViewHolder(holder: RecordHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) super.onBindViewHolder(holder, position, payloads)
        val item = getItem(position)
        payloads.forEach { payload ->
            if (payload is List<*>) {
                if (payload.contains(GridDiffUtil.OBJECT_HEADER_CHANGED)) {
                    holder.bindObjectHeader(item)
                }
                holder.bindObjectCells(item)
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecordHolder) {
        super.onViewAttachedToWindow(holder)
        holder.root.headerContainer.translationX = recordNamePositionX
    }

    fun clear() {
        recordNamePositionX = 0f
    }

    class RecordHolder(view: View) : RecyclerView.ViewHolder(view) {

        val root: LinearLayout = itemView.holderRoot
        val adapter get() = itemView.rowCellRecycler.adapter as ViewerGridCellsAdapter

        fun bindObjectHeader(row: Viewer.GridView.Row) {
            Timber.d("Binding object header")
            itemView.objectIcon.setIcon(
                emoji = row.emoji,
                image = row.image,
                name = row.name.orEmpty()
            )
            itemView.tvTitle.text = row.name
        }

        fun bindObjectCells(row: Viewer.GridView.Row) {
            Timber.d("Binding object cells")
            adapter.update(row.cells)
        }
    }

    object GridDiffUtil : DiffUtil.ItemCallback<Viewer.GridView.Row>() {
        override fun areItemsTheSame(
            oldItem: Viewer.GridView.Row,
            newItem: Viewer.GridView.Row
        ): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(
            oldItem: Viewer.GridView.Row,
            newItem: Viewer.GridView.Row
        ): Boolean = oldItem == newItem

        override fun getChangePayload(
            oldItem: Viewer.GridView.Row,
            newItem: Viewer.GridView.Row
        ): Any? {
            val payload = mutableListOf<Int>()
            if (oldItem.emoji != newItem.emoji || oldItem.image != newItem.image || oldItem.name != newItem.name) {
                payload.add(OBJECT_HEADER_CHANGED)
            }
            return payload
        }

        const val OBJECT_HEADER_CHANGED = 0
    }
}