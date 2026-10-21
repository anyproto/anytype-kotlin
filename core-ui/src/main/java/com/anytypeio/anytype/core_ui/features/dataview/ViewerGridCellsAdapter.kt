package com.anytypeio.anytype.core_ui.features.dataview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemViewerGridCellDescriptionBinding
import com.anytypeio.anytype.core_ui.databinding.ItemViewerGridCellFileBinding
import com.anytypeio.anytype.core_ui.databinding.ItemViewerGridCellObjectBinding
import com.anytypeio.anytype.core_ui.databinding.ItemViewerGridCellTagBinding
import com.anytypeio.anytype.core_ui.features.dataview.diff.CellViewDiffUtil
import com.anytypeio.anytype.core_ui.features.dataview.holders.*
import com.anytypeio.anytype.presentation.sets.model.CellView

class ViewerGridCellsAdapter(
    var cells: List<CellView> = listOf(),
    private val onCellClicked: (CellView) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun update(update: List<CellView>) {
        val diff = DiffUtil.calculateDiff(CellViewDiffUtil(old = cells, new = update), false)
        cells = update
        diff.dispatchUpdatesTo(this)
    }

    /**
     * Resolves the clicked cell through the holder's CURRENT binding adapter, not the adapter
     * that created it: all row cell recyclers share one RecycledViewPool, so a holder created
     * by one row's adapter is routinely rebound by another row's adapter. Reading the creating
     * adapter's [cells] here would dispatch the click against a different row's cell.
     * [onCellClicked] itself is safe to capture — every row adapter is constructed with the
     * same callback instance.
     */
    private fun RecyclerView.ViewHolder.setCellClickListener() {
        itemView.setOnClickListener {
            val pos = bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                (bindingAdapter as? ViewerGridCellsAdapter)
                    ?.cells
                    ?.getOrNull(pos)
                    ?.let(onCellClicked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            HOLDER_DESCRIPTION -> {
                DVGridCellDescriptionHolder(
                    view = inflater.inflate(
                        R.layout.item_viewer_grid_cell_description,
                        parent,
                        false
                    )
                ).apply {
                    setCellClickListener()
                }
            }
            HOLDER_DATE -> {
                DVGridCellDateHolder(
                    view = inflater.inflate(
                        R.layout.item_viewer_grid_cell_description,
                        parent,
                        false
                    )
                ).apply {
                    setCellClickListener()
                }
            }
            HOLDER_NUMBER -> {
                DVGridCellNumberHolder(
                    view = inflater.inflate(
                        R.layout.item_viewer_grid_cell_description,
                        parent,
                        false
                    )
                ).apply {
                    setCellClickListener()
                }
            }
            HOLDER_EMAIL -> {
                DVGridCellEmailHolder(
                    view = inflater.inflate(
                        R.layout.item_viewer_grid_cell_description,
                        parent,
                        false
                    )
                ).apply {
                    setCellClickListener()
                }
            }
            HOLDER_URL -> {
                DVGridCellUrlHolder(
                    view = inflater.inflate(
                        R.layout.item_viewer_grid_cell_description,
                        parent,
                        false
                    )
                ).apply {
                    setCellClickListener()
                }
            }
            HOLDER_PHONE -> {
                DVGridCellPhoneHolder(
                    view = inflater.inflate(
                        R.layout.item_viewer_grid_cell_description,
                        parent,
                        false
                    )
                ).apply {
                    setCellClickListener()
                }
            }
            HOLDER_CHECKBOX -> {
                DVGridCellCheckboxHolder(
                    view = inflater.inflate(
                        R.layout.item_viewer_grid_cell_checkbox,
                        parent,
                        false
                    )
                ).apply {
                    setCellClickListener()
                }
            }
            HOLDER_TAG -> {
                DVGridCellTagHolder(
                    ItemViewerGridCellTagBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    setCellClickListener()
                }
            }
            HOLDER_STATUS -> {
                DVGridCellStatusHolder(
                    ItemViewerGridCellDescriptionBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    setCellClickListener()
                }
            }
            HOLDER_OBJECT -> {
                DVGridCellObjectHolder(
                    ItemViewerGridCellObjectBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    setCellClickListener()
                }
            }
            HOLDER_FILE -> {
                DVGridCellFileHolder(
                    ItemViewerGridCellFileBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    setCellClickListener()
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun getItemCount(): Int = cells.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DVGridCellDescriptionHolder -> holder.bind(cells[position] as CellView.Description)
            is DVGridCellDateHolder -> holder.bind(cells[position] as CellView.Date)
            is DVGridCellNumberHolder -> holder.bind(cells[position] as CellView.Number)
            is DVGridCellEmailHolder -> holder.bind(cells[position] as CellView.Email)
            is DVGridCellUrlHolder -> holder.bind(cells[position] as CellView.Url)
            is DVGridCellPhoneHolder -> holder.bind(cells[position] as CellView.Phone)
            is DVGridCellTagHolder -> holder.bind(cells[position] as CellView.Tag)
            is DVGridCellStatusHolder -> holder.bind(cells[position] as CellView.Status)
            is DVGridCellObjectHolder -> holder.bind(cells[position] as CellView.Object)
            is DVGridCellFileHolder -> holder.bind(cells[position] as CellView.File)
            is DVGridCellCheckboxHolder -> holder.bind(cells[position] as CellView.Checkbox)
        }
    }

    override fun getItemViewType(position: Int): Int = when (cells[position]) {
        is CellView.Description -> HOLDER_DESCRIPTION
        is CellView.Date -> HOLDER_DATE
        is CellView.Number -> HOLDER_NUMBER
        is CellView.Email -> HOLDER_EMAIL
        is CellView.Url -> HOLDER_URL
        is CellView.Phone -> HOLDER_PHONE
        is CellView.Tag -> HOLDER_TAG
        is CellView.Status -> HOLDER_STATUS
        is CellView.Object -> HOLDER_OBJECT
        is CellView.File -> HOLDER_FILE
        is CellView.Checkbox -> HOLDER_CHECKBOX
        else -> throw IllegalStateException("Unexpected view type: ${cells[position]}")
    }

    companion object {
        const val HOLDER_DESCRIPTION = 1
        const val HOLDER_DATE = 2
        const val HOLDER_NUMBER = 3
        const val HOLDER_EMAIL = 4
        const val HOLDER_URL = 5
        const val HOLDER_PHONE = 6
        const val HOLDER_TAG = 7
        const val HOLDER_STATUS = 8
        const val HOLDER_OBJECT = 9
        const val HOLDER_FILE = 10
        const val HOLDER_CHECKBOX = 11
    }
}