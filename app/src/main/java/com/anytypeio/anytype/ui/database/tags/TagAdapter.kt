package com.anytypeio.anytype.ui.database.tags

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.databaseview.models.TagView
import com.anytypeio.anytype.ui.database.table.adapter.toView

class TagAdapter : RecyclerView.Adapter<TagViewHolder>() {

    private val tags = mutableListOf<TagView>()

    fun addData(tags: List<TagView>) {
        this.tags.addAll(tags)
        notifyDataSetChanged()
    }

    fun clear() {
        val size = this.tags.size
        this.tags.clear()
        notifyItemRangeRemoved(0, size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder =
        TagViewHolder(
            LayoutInflater.from(
                parent.context
            ).toView(R.layout.item_tag, parent)
        )

    override fun getItemCount(): Int = tags.size

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(tags[position])
    }
}