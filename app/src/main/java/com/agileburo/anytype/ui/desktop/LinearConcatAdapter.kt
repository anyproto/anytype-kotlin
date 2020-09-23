package com.agileburo.anytype.ui.desktop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.R
import kotlinx.android.synthetic.main.item_profile_container.view.*

class LinearConcatAdapter(val adapter: DashboardProfileAdapter) :
    RecyclerView.Adapter<LinearConcatAdapter.LinearConcatHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinearConcatHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_profile_container, parent, false)
        view.recyclerView.apply {
            layoutManager = LinearLayoutManager(parent.context)
            val lp = (layoutParams as FrameLayout.LayoutParams)
            lp.height = (parent.height / 2) - lp.topMargin - lp.bottomMargin
        }
        return LinearConcatHolder(view)
    }

    override fun onBindViewHolder(holder: LinearConcatHolder, position: Int) {
        holder.bind(adapter)
    }

    override fun getItemCount(): Int = 1

    class LinearConcatHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(adapter: DashboardProfileAdapter) {
            itemView.recyclerView.adapter = adapter
        }
    }
}