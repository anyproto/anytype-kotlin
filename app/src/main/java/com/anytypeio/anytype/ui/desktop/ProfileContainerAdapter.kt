package com.anytypeio.anytype.ui.desktop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import kotlinx.android.synthetic.main.item_profile_container.view.*

class ProfileContainerAdapter(
    val adapter: DashboardProfileAdapter
) : RecyclerView.Adapter<ProfileContainerAdapter.ProfileContainerHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileContainerHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_profile_container, parent, false)
        view.recyclerView.apply {
            layoutManager = LinearLayoutManager(parent.context)
            val lp = (layoutParams as FrameLayout.LayoutParams)
            lp.height = (parent.height / 2) - lp.topMargin - lp.bottomMargin
        }
        return ProfileContainerHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileContainerHolder, position: Int) {
        holder.bind(adapter)
    }

    override fun getItemCount(): Int = 1

    class ProfileContainerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(adapter: DashboardProfileAdapter) {
            itemView.recyclerView.adapter = adapter
        }
    }
}