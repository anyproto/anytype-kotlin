package com.agileburo.anytype.feature_editor.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_utils.swap
import com.agileburo.anytype.feature_editor.domain.Block
import timber.log.Timber

class EditorAdapter(private val blocks: MutableList<Block>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun setBlocks(blocks: List<Block>) {
        Timber.d("Set blocks ${blocks.size}")
        this.blocks.addAll(blocks)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

    }

    override fun getItemCount() = blocks.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    fun onItemMoved(fromPosition: Int, toPosition: Int): Boolean {
        swapPosition(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    private fun swapPosition(fromPosition: Int, toPosition: Int) {
        blocks.swap(fromPosition, toPosition)
    }

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        class TextHolder(itemView: View) : ViewHolder(itemView)

    }
}