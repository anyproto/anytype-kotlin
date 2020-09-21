package com.agileburo.anytype.ui.desktop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.extensions.avatarColor
import com.agileburo.anytype.core_utils.ext.firstDigitByHash
import com.agileburo.anytype.core_utils.ext.typeOf
import com.agileburo.anytype.presentation.desktop.DashboardView
import kotlinx.android.synthetic.main.item_dashboard_profile_header.view.*

class DashboardProfileAdapter(
    private var data: MutableList<DashboardView>,
    private val onProfileClicked: () -> Unit
) : RecyclerView.Adapter<DashboardProfileAdapter.ProfileHolder>() {

    fun update(views: List<DashboardView>) {
        data.clear()
        data.addAll(views)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ProfileHolder(inflater.inflate(R.layout.item_dashboard_profile_header, parent, false))
    }

    override fun onBindViewHolder(holder: ProfileHolder, position: Int) {
        val item = data[position] as DashboardView.Profile
        with(holder) {
            bindClick(onProfileClicked)
            bindName(item.name)
            bindAvatar(name = item.name, avatar = item.avatar)
        }
    }

    override fun onBindViewHolder(
        holder: ProfileHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            payloads.typeOf<DesktopDiffUtil.Payload>().forEach { payload ->
                val item = data[position] as DashboardView.Profile
                with(holder) {
                    if (payload.titleChanged()) {
                        bindName(item.name)
                    }
                    if (payload.imageChanged()) {
                        bindAvatar(item.name, item.avatar)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = data.size

    class ProfileHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindName(name: String) {
            itemView.greeting.text = itemView.context.getString(R.string.greet, name)
        }

        fun bindAvatar(name: String, avatar: String?) {
            val pos = name.firstDigitByHash()
            itemView.avatar.bind(
                name = name,
                color = itemView.context.avatarColor(pos)
            )
            avatar?.let { itemView.avatar.icon(it) }
        }

        fun bindClick(onClick: () -> Unit) {
            itemView.avatar.setOnClickListener { onClick() }
        }
    }
}