package com.anytypeio.anytype.core_ui.features.dataview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.dataview.diff.CellViewDiffUtil
import com.anytypeio.anytype.core_ui.features.dataview.holders.*
import com.anytypeio.anytype.presentation.sets.CellAction
import com.anytypeio.anytype.presentation.sets.model.CellView

class ViewerGridCellsAdapter(
    private var cells: List<CellView> = listOf(),
    private val onCellClicked: (CellView) -> Unit,
    private val onCellAction: (CellAction) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun update(update: List<CellView>) {
        // TODO maybe disable detectMoves
        val diff = DiffUtil.calculateDiff(CellViewDiffUtil(old = cells, new = update))
        cells = update
        diff.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        //todo Take cells width from columns width property
        return when (viewType) {
            HOLDER_DESCRIPTION -> {
                DVGridCellDescriptionHolder(
                    view = inflater.inflate(
                        R.layout.item_viewer_grid_cell_description,
                        parent,
                        false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        onCellClicked(cells[bindingAdapterPosition])
                    }
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
                    itemView.setOnClickListener {
                        onCellClicked(cells[bindingAdapterPosition])
                    }
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
                    itemView.setOnClickListener {
                        onCellClicked(cells[bindingAdapterPosition])
                    }
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
                    itemView.setOnClickListener {
                        onCellClicked(cells[bindingAdapterPosition])
                    }
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
                    itemView.setOnClickListener {
                        onCellClicked(cells[bindingAdapterPosition])
                    }
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
                    itemView.setOnClickListener {
                        onCellClicked(cells[bindingAdapterPosition])
                    }
                }
            }
            HOLDER_TAG -> {
                DVGridCellTagHolder(
                    view = inflater.inflate(R.layout.item_viewer_grid_cell_tag, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        onCellClicked(cells[bindingAdapterPosition])
                    }
                }
            }
            HOLDER_STATUS -> {
                DVGridCellStatusHolder(
                    view = inflater.inflate(
                        R.layout.item_viewer_grid_cell_description,
                        parent,
                        false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        onCellClicked(cells[bindingAdapterPosition])
                    }
                }
            }
            HOLDER_OBJECT -> {
                DVGridCellObjectHolder(
                    view = inflater.inflate(
                        R.layout.item_viewer_grid_cell_object,
                        parent,
                        false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        onCellClicked(cells[bindingAdapterPosition])
                    }
                }
            }
            HOLDER_FILE -> {
                DVGridCellFileHolder(
                    view = inflater.inflate(
                        R.layout.item_viewer_grid_cell_file,
                        parent,
                        false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        onCellClicked(cells[bindingAdapterPosition])
                    }
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
    }
}