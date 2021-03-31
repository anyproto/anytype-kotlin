package com.anytypeio.anytype.core_ui.features.dataview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.dataview.diff.GridRowDiffUtil
import com.anytypeio.anytype.presentation.sets.CellAction
import com.anytypeio.anytype.presentation.sets.model.CellView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import kotlinx.android.synthetic.main.item_viewer_grid_row.view.*

class ViewerGridAdapter(
    private var items: List<Viewer.GridView.Row> = emptyList(),
    private val onCellClicked: (CellView) -> Unit,
    private val onCellAction: (CellAction) -> Unit,
    private val onObjectHeaderClicked: (String, String) -> Unit
) : RecyclerView.Adapter<ViewerGridAdapter.RecordHolder>() {

    var recordNamePositionX = 0f

    fun update(update: List<Viewer.GridView.Row>) {
        val diff = DiffUtil.calculateDiff(GridRowDiffUtil(old = items, new = update))
        items = update
        diff.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecordHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_viewer_grid_row, parent, false)
        view.rowCellRecycler.apply {
            adapter = ViewerGridCellsAdapter(
                onCellClicked = onCellClicked,
                onCellAction = onCellAction
            )
            //addItemDecoration(DividerItemDecoration(parent.context, LinearLayout.HORIZONTAL))
        }
        return RecordHolder(view).apply {
            itemView.headerContainer.setOnClickListener {
                val item = items[bindingAdapterPosition]
                onObjectHeaderClicked(item.id, item.type)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecordHolder, position: Int) {
        holder.bind(items[position])
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

        fun bind(
            row: Viewer.GridView.Row
        ) {
            itemView.objectIcon.setIcon(
                emoji = row.emoji,
                image = row.image,
                name = row.name.orEmpty()
            )
            itemView.tvTitle.text = row.name
            adapter.update(row.cells)
        }
    }
}