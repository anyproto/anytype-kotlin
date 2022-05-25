package com.anytypeio.anytype.core_utils.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_utils.R

class ItemClickSupport private constructor(private val recyclerView: RecyclerView) {

    companion object {
        fun addTo(view: RecyclerView): ItemClickSupport {
            var support: ItemClickSupport? = view.getTag(R.id.item_click_support) as? ItemClickSupport
            if (support == null) {
                support = ItemClickSupport(view)
            }
            return support
        }

        fun removeFrom(view: RecyclerView): ItemClickSupport {
            val support = view.getTag(R.id.item_click_support) as ItemClickSupport
            support.detach(view)
            return support
        }
    }

    private var onItemClick: ((Int) -> Unit)? = null
    private var onLongItemClick: ((Int) -> Boolean)? = null

    private val onClickListener = View.OnClickListener { v ->
        val holder = recyclerView.getChildViewHolder(v)
        onItemClick?.invoke(holder.adapterPosition)
    }

    private val onLongClickListener = View.OnLongClickListener { v ->
        val holder = recyclerView.getChildViewHolder(v)
        onLongItemClick?.invoke(holder.adapterPosition) ?: false
    }

    private val attachListener = object : RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewAttachedToWindow(view: View) {
            if (onItemClick != null) {
                view.setOnClickListener(onClickListener)
            }
            if (onLongItemClick != null) {
                view.setOnLongClickListener(onLongClickListener)
            }
        }

        override fun onChildViewDetachedFromWindow(view: View) {
        }
    }

    init {
        recyclerView.setTag(R.id.item_click_support, this)
        recyclerView.addOnChildAttachStateChangeListener(attachListener)
    }

    fun onItemClick(onItemClick: (Int) -> Unit): ItemClickSupport {
        this.onItemClick = onItemClick
        return this
    }

    fun onLongItemClick(onLongItemClick: (Int) -> Boolean): ItemClickSupport {
        this.onLongItemClick = onLongItemClick
        return this
    }

    private fun detach(view: RecyclerView) {
        view.removeOnChildAttachStateChangeListener(attachListener)
        view.setTag(R.id.item_click_support, null)
    }
}