package com.anytypeio.anytype.ui.database.tags

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.presentation.databaseview.models.TagView
import kotlinx.android.synthetic.main.item_tag.view.*

class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(tag: TagView) {
        itemView.name.text = tag.name
        itemView.backgroundTintList = ColorStateList.valueOf(randomColor(tag.name))
    }

    private fun randomColor(name: String): Int {
        var hash = 0

        for (i in name.indices) {
            hash = name[i].toInt() + ((hash.shl(5) - hash))
        }

        val h = (hash % 360).toFloat()

        return Color.HSVToColor(floatArrayOf(h, 0.3f, 0.8f))
    }
}