package com.anytypeio.anytype.core_ui.features.dataview.modals

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.sets.model.TagValue

class TagAdapter(private val tags: List<TagValue>) : RecyclerView.Adapter<TagAdapter.TagHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagHolder {
        return TagHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.modal_item_filter_tag_value, parent, false)
        )
    }

    override fun onBindViewHolder(holder: TagHolder, position: Int) {
        holder.bind(tags[position])
    }

    override fun getItemCount(): Int = tags.size

    inner class TagHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(tag: TagValue) {
            itemView.findViewById<TextView>(R.id.tagTitle).text = tag.text
        }
    }
}