package com.anytypeio.anytype.core_ui.features.`object`

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.`object`.ObjectAction
import kotlinx.android.synthetic.main.item_object_menu_action.view.*

class ObjectActionAdapter(
    val onObjectActionClicked: (ObjectAction) -> Unit
) : RecyclerView.Adapter<ObjectActionAdapter.ViewHolder>() {

    var items: List<ObjectAction> = emptyList()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent).apply {
        itemView.setOnClickListener {
            val pos = bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION)
                onObjectActionClicked(items[pos])
        }
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_object_menu_action, parent, false
        )
    ) {
        fun bind(item: ObjectAction) = with(itemView) {
            when (item) {
                ObjectAction.DELETE -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_archive)
                    tvActionTitle.setText(R.string.archive)
                }
                ObjectAction.ADD_TO_FAVOURITE -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_add_to_favorites)
                    tvActionTitle.setText(R.string.favourite)
                }
                ObjectAction.SEARCH_ON_PAGE -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_search)
                    tvActionTitle.setText(R.string.search)
                }
                ObjectAction.USE_AS_TEMPLATE -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_template)
                    tvActionTitle.setText(R.string.template)
                }
                ObjectAction.MOVE_TO -> TODO()
            }
        }
    }
}