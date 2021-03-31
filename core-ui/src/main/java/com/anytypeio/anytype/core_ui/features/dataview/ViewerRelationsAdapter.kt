package com.anytypeio.anytype.core_ui.features.dataview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.relationIcon
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import kotlinx.android.synthetic.main.item_viewer_relation_list.view.*

class ViewerRelationsAdapter(
    private val onSwitchClick: (SimpleRelationView) -> Unit
) : RecyclerView.Adapter<ViewerRelationsAdapter.Holder>() {

    private var items: List<SimpleRelationView> = emptyList()

    fun update(update: List<SimpleRelationView>) {
        items = update
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_viewer_relation_list, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position], onSwitchClick)
    }

    /**
     * https://stackoverflow.com/questions/38543196/strange-recyclerview-checkbox-oncheckchanged-behavior
     */
    override fun onViewRecycled(holder: Holder) {
        super.onViewRecycled(holder)
        holder.itemView.switchView.setOnCheckedChangeListener(null)
    }

    override fun getItemCount(): Int = items.size

    class Holder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: SimpleRelationView, onSwitchClick: (SimpleRelationView) -> Unit) {
            if (item.key == ObjectSetConfig.NAME_KEY) {
                itemView.switchView.invisible()
            } else {
                itemView.switchView.visible()
            }
            itemView.iconRelation.setBackgroundResource(item.format.relationIcon())
            itemView.title.text = item.title
            itemView.switchView.isChecked = item.isVisible
            itemView.switchView.setOnCheckedChangeListener { _, isChecked ->
                onSwitchClick(item.copy(isVisible = isChecked))
            }
        }
    }
}