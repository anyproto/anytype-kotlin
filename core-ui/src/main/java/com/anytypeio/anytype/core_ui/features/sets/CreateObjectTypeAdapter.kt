package com.anytypeio.anytype.core_ui.features.sets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.sets.CreateObjectTypeView
import kotlinx.android.synthetic.main.item_object_type.view.*

class CreateObjectTypeAdapter(
    private val onObjectTypeClick: (Int) -> Unit
) : RecyclerView.Adapter<CreateObjectTypeAdapter.ViewHolder>() {

    var types: List<CreateObjectTypeView> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            view = inflater.inflate(
                R.layout.item_object_type,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = types.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(types[position], onObjectTypeClick)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(
            type: CreateObjectTypeView,
            onObjectTypeClick: (Int) -> Unit
        ) {
            itemView.setOnClickListener {
                onObjectTypeClick(type.layout)
            }
            itemView.title.text = type.name
            itemView.isSelected = type.isSelected
        }
    }
}