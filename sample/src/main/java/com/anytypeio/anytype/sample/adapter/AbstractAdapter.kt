package com.anytypeio.anytype.sample.adapter

import androidx.recyclerview.widget.RecyclerView

abstract class AbstractAdapter<T>(
    private var items: List<T>
) : RecyclerView.Adapter<AbstractHolder<T>>() {

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: AbstractHolder<T>, position: Int) {
        holder.bind(items[position])
    }

    abstract fun update(update: List<T>)
}