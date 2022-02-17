package com.anytypeio.anytype.core_ui.features.relations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.ItemEditCellObjectBinding
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
        return ObjectRelationObjectHolder(
            binding = ItemEditCellObjectBinding.inflate(
                inflater, parent, false
            )
        ).apply {
            itemView.setOnClickListener {
                val item = views[bindingAdapterPosition]
                onObjectClick(item.id)
            }
        }
    }

    override fun onBindViewHolder(holder: ObjectRelationObjectHolder, position: Int) {
        val view = views[position]
        if (view is RelationValueView.Object.Default) {
            holder.bind(view)
        }
    }

    override fun getItemCount(): Int = views.size
}