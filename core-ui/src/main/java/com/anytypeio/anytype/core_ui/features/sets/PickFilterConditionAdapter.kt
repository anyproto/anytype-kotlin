package com.anytypeio.anytype.core_ui.features.sets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.sets.model.Viewer
import kotlinx.android.synthetic.main.item_dv_viewer_filter_condition.view.*

class PickFilterConditionAdapter(
    private val conditions: List<Viewer.Filter.Condition>,
    private val picked: Viewer.Filter.Condition?,
    private val click: (Viewer.Filter.Condition) -> Unit
) : RecyclerView.Adapter<PickFilterConditionAdapter.ConditionHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConditionHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dv_viewer_filter_condition, parent, false)
        return ConditionHolder(view = view).apply {
            itemView.root.setOnClickListener {
                click.invoke(conditions[bindingAdapterPosition])
            }
        }
    }

    override fun onBindViewHolder(holder: ConditionHolder, position: Int) {
        holder.bind(
            isChecked = conditions[position].title == picked?.title,
            condition = conditions[position]
        )
    }

    override fun getItemCount(): Int = conditions.size

    inner class ConditionHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(
            isChecked: Boolean,
            condition: Viewer.Filter.Condition,
        ) {
            if (isChecked) {
                itemView.iconCheck.visible()
            } else {
                itemView.iconCheck.invisible()
            }
            itemView.tvCondition.text = condition.title
        }
    }
}