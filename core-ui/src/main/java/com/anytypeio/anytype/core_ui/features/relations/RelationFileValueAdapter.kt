package com.anytypeio.anytype.core_ui.features.relations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.ItemEditCellFileBinding
import com.anytypeio.anytype.core_ui.features.relations.holders.RelationFileHolder
import com.anytypeio.anytype.presentation.relations.RelationValueView

class RelationFileValueAdapter(
    private val onFileClick: (String) -> Unit
) :
    RecyclerView.Adapter<RelationFileHolder>() {

    private var views = emptyList<RelationValueView.File>()

    fun update(update: List<RelationValueView.File>) {
        views = update
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelationFileHolder {
        val inflater = LayoutInflater.from(parent.context)
        return RelationFileHolder(
            binding = ItemEditCellFileBinding.inflate(
                inflater, parent, false
            )
        ).apply {
            itemView.setOnClickListener {
                val item = views[bindingAdapterPosition]
                onFileClick(item.id)
            }
        }
    }

    override fun onBindViewHolder(holder: RelationFileHolder, position: Int) {
        holder.bind(views[position])
    }

    override fun getItemCount(): Int = views.size
}