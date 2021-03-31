package com.anytypeio.anytype.core_ui.features.sets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.presentation.sets.model.Viewer
import kotlinx.android.synthetic.main.item_list_base.view.*

class PickSortingTypeAdapter(
    private val items: List<Viewer.SortType>,
    private val keySelected: String,
    private val typeSelected: Int,
    private val click: (String, Viewer.SortType) -> Unit
) : RecyclerView.Adapter<PickSortingTypeAdapter.SortHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SortHolder {
        return SortHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_list_base, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SortHolder, position: Int) {
        holder.bind(
            key = keySelected,
            item = items[position],
            type = typeSelected,
            click = click
        )
    }

    override fun getItemCount(): Int = items.size

    class SortHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(
            key: String,
            type: Int,
            item: Viewer.SortType,
            click: (String, Viewer.SortType) -> Unit
        ) {
            with(itemView) {
                icon.gone()
                text.text = item.name
                isSelected = item.ordinal == type
                setOnClickListener {
                    click(key, item)
                }
            }
        }
    }
}