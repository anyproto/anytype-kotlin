package com.anytypeio.anytype.core_ui.features.relations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.relations.holders.ObjectRelationObjectHolder
import com.anytypeio.anytype.presentation.sets.RelationValueBaseViewModel.RelationValueView

class RelationObjectValueAdapter(
    private val onObjectClick: (String) -> Unit
) : RecyclerView.Adapter<ObjectRelationObjectHolder>() {

    private var views = emptyList<RelationValueView.Object>()

    fun update(update: List<RelationValueView.Object>) {
        views = update
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObjectRelationObjectHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_edit_cell_object, parent, false)
        return ObjectRelationObjectHolder(view).apply {
            itemView.setOnClickListener {
                val item = views[bindingAdapterPosition]
                onObjectClick(item.id)
            }
        }
    }

    override fun onBindViewHolder(holder: ObjectRelationObjectHolder, position: Int) {
        holder.bind(views[position])
    }

    override fun getItemCount(): Int = views.size
}