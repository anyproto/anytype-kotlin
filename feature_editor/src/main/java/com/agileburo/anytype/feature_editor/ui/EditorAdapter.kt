package com.agileburo.anytype.feature_editor.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_utils.swap
import com.agileburo.anytype.feature_editor.R
import com.agileburo.anytype.feature_editor.domain.Block
import kotlinx.android.synthetic.main.item_block_editable.view.*
import timber.log.Timber

class EditorAdapter(private val blocks: MutableList<Block>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun setBlocks(blocks: List<Block>) {
        Timber.d("Set blocks ${blocks.size}")
        this.blocks.addAll(blocks)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_block_editable, parent, false)
        return ViewHolder.TextHolder(view)
    }

    override fun getItemCount() = blocks.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.TextHolder -> holder.bind(blocks[position])
        }
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

        class TextHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(block : Block) {
                itemView.tvId.text = "id :${block.id}"
                itemView.tvContent.text = block.content
            }
        }

    }
}