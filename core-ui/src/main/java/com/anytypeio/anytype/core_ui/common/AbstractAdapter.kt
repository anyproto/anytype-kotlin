package com.anytypeio.anytype.core_ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class AbstractAdapter<T>(
    var items: List<T>
) : RecyclerView.Adapter<AbstractViewHolder<T>>() {

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: AbstractViewHolder<T>, position: Int) {
        holder.bind(items[position])
    }

    fun inflate(parent: ViewGroup, res: Int): View = LayoutInflater.from(parent.context).inflate(
        res,
        parent,
        false
    )

    open fun update(update: List<T>) {
        items = update
        notifyDataSetChanged()
    }
}