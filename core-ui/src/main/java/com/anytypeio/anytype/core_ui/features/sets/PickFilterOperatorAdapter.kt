package com.anytypeio.anytype.core_ui.features.sets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.ItemListBaseBinding
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.presentation.sets.model.Viewer

class PickFilterOperatorAdapter(
    private val operators: List<Viewer.FilterOperator>,
    private val click: (Viewer.FilterOperator) -> Unit
) : RecyclerView.Adapter<PickFilterOperatorAdapter.OperatorHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperatorHolder {
        return OperatorHolder(
            binding = ItemListBaseBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OperatorHolder, position: Int) {
        holder.bind(
            operator = operators[position],
            click = click
        )
    }

    override fun getItemCount(): Int = operators.size

    inner class OperatorHolder(
        val binding: ItemListBaseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            operator: Viewer.FilterOperator,
            click: (Viewer.FilterOperator) -> Unit
        ) = with(binding) {
            icon.gone()
            text.text = operator.name
            itemView.setOnClickListener {
                click(operator)
            }
        }
    }
}