package com.anytypeio.anytype.core_ui.features.dataview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.sets.model.CellView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import kotlinx.android.synthetic.main.item_viewer_grid_row.view.*

class ViewerGridAdapter(
    private val onCellClicked: (CellView) -> Unit,
    private val onObjectHeaderClicked: (Id) -> Unit,
    private val onTaskCheckboxClicked : (Id) -> Unit
) : ListAdapter<Viewer.GridView.Row, ViewerGridAdapter.RecordHolder>(GridDiffUtil) {

    var recordNamePositionX = 0f

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecordHolder {
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
                    onObjectHeaderClicked(item.id)
                }
            }
            itemView.objectIcon.checkbox.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val item = getItem(pos)
                    onTaskCheckboxClicked(item.id)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecordHolder, position: Int) {
        val item = getItem(position)
        holder.bindObjectHeader(item)
        holder.bindObjectCells(item)
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
            when (row.layout) {
                ObjectType.Layout.TODO -> {
                    itemView.objectIcon.visible()
                    itemView.objectIcon.setCheckbox(row.isChecked)
                }
                ObjectType.Layout.BASIC -> {
                    if (row.image != null || row.emoji != null) {
                        itemView.objectIcon.visible()
                        if (row.image != null) {
                            itemView.objectIcon.setRectangularImage(row.image)
                        } else if (row.emoji != null) {
                            itemView.objectIcon.setEmoji(row.emoji)
                        }
                    } else {
                        itemView.objectIcon.gone()
                    }
                }
                ObjectType.Layout.PROFILE -> {
                    itemView.objectIcon.visible()
                    if (row.image != null) {
                        itemView.objectIcon.setCircularImage(row.image)
                    } else {
                        itemView.objectIcon.setProfileInitials(row.name.orEmpty())
                    }
                }
                else -> {
                    itemView.objectIcon.gone()
                }
            }
            itemView.tvTitle.text = row.name
        }

        fun bindObjectCells(row: Viewer.GridView.Row) {
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
        ): Any {
            val payload = mutableListOf<Int>()
            if (isHeaderChanged(oldItem, newItem)) payload.add(OBJECT_HEADER_CHANGED)
            return payload
        }

        private fun isHeaderChanged(
            oldItem: Viewer.GridView.Row,
            newItem: Viewer.GridView.Row
        ) = (oldItem.emoji != newItem.emoji
                || oldItem.image != newItem.image
                || oldItem.name != newItem.name
                || oldItem.isChecked != newItem.isChecked
                || oldItem.layout != newItem.layout)

        const val OBJECT_HEADER_CHANGED = 0
    }
}