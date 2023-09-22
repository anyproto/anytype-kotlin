package com.anytypeio.anytype.core_ui.features.dataview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.ItemViewerRelationListBinding
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.sets.model.ViewerRelationListView

class ViewerRelationsAdapter(
    private val onSwitchClick: (SimpleRelationView) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<ViewerRelationListView> = emptyList()

    fun update(update: List<ViewerRelationListView>) {
        items = update
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_RELATION -> {
                Holder(
                    ItemViewerRelationListBinding.inflate(
                        inflater, parent, false
                    )
                )
            }
            else -> throw IllegalStateException("Unexpected view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is Holder -> {
                val item = items[position] as ViewerRelationListView.Relation
                holder.bind(item.view, onSwitchClick)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ViewerRelationListView.Relation -> VIEW_TYPE_RELATION
    }

    /**
     * https://stackoverflow.com/questions/38543196/strange-recyclerview-checkbox-oncheckchanged-behavior
     */
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is Holder) {
            holder.binding.switchView.setOnCheckedChangeListener(null)
        }
    }

    override fun getItemCount(): Int = items.size

    class Holder(val binding: ItemViewerRelationListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: SimpleRelationView,
            onSwitchClick: (SimpleRelationView) -> Unit
        ) = with(binding) {
            if (item.key == ObjectSetConfig.NAME_KEY) {
                switchView.invisible()
            } else {
                switchView.visible()
            }
            iconRelation.bind(item.format)
            title.text = item.title
            switchView.isChecked = item.isVisible
            switchView.setOnCheckedChangeListener { _, isChecked ->
                onSwitchClick(item.copy(isVisible = isChecked))
            }
        }
    }

    companion object {
        const val VIEW_TYPE_RELATION = 1
    }
}