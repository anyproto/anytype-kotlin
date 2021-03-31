package com.anytypeio.anytype.core_ui.features.sets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.presentation.sets.model.Viewer
import kotlinx.android.synthetic.main.item_list_base.view.*

class PickFilterOperatorAdapter(
    private val operators: List<Viewer.FilterOperator>,
    private val click: (Viewer.FilterOperator) -> Unit
) : RecyclerView.Adapter<PickFilterOperatorAdapter.OperatorHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperatorHolder {
        return OperatorHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_list_base, parent, false)
        )
    }

    override fun onBindViewHolder(holder: OperatorHolder, position: Int) {
        holder.bind(
            operator = operators[position],
            click = click
        )
    }

    override fun getItemCount(): Int = operators.size

    inner class OperatorHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(
            operator: Viewer.FilterOperator,
            click: (Viewer.FilterOperator) -> Unit
        ) {
            with(itemView) {
                icon.gone()
                text.text = operator.name
                setOnClickListener {
                    click(operator)
                }
            }
        }
    }
}