package com.anytypeio.anytype.core_ui.features.sets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R

class CreateSetHeaderAdapter(
    private val onCreateNewObjectTypeClicked: () -> Unit
) : RecyclerView.Adapter<CreateSetHeaderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            view = inflater.inflate(
                R.layout.item_create_set_header,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(onCreateNewObjectTypeClicked)
    }

    override fun getItemCount(): Int = 1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val container: ConstraintLayout = itemView.findViewById(R.id.createNewContainer)

        fun bind(onCreateNewObjectTypeClicked: () -> Unit) {
            container.setOnClickListener {
                onCreateNewObjectTypeClicked()
            }
        }
    }
}