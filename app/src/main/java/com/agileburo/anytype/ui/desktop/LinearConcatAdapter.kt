package com.agileburo.anytype.ui.desktop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.R
import kotlinx.android.synthetic.main.item_profile_container.view.*

class LinearConcatAdapter(val adapter: DashboardProfileAdapter) :
    RecyclerView.Adapter<LinearConcatAdapter.LinearConcatHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinearConcatHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_profile_container, parent, false)
        view.recyclerView.layoutManager = LinearLayoutManager(parent.context)
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