package com.agileburo.anytype

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import anytype.io.mobile.model.Block

class DocumentAdapter(private val blocks : MutableList<Block>) : RecyclerView.Adapter<DocumentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_text_holder, parent, false)
        return ViewHolder.TextHolder(view)
    }

    override fun getItemCount(): Int {
        return blocks.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }

    fun onItemMoved(fromPosition: Int, toPosition: Int) : Boolean {
        swapPosition(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    private fun swapPosition(fromPosition: Int, toPosition: Int) {
        blocks.swap(fromPosition, toPosition)
    }

    sealed class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        class TextHolder(itemView : View) : ViewHolder(itemView)

    }

}