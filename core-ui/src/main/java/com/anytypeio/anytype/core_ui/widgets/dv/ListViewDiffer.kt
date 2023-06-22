package com.anytypeio.anytype.core_ui.widgets.dv

import androidx.recyclerview.widget.DiffUtil
import com.anytypeio.anytype.presentation.sets.model.Viewer

object ListViewDiffer : DiffUtil.ItemCallback<Viewer.ListView.Item>() {

    const val PAYLOAD_CHECKED = 1
    const val PAYLOAD_NAME = 2
    const val PAYLOAD_ICON = 3
    const val PAYLOAD_DESCRIPTION = 4
    const val PAYLOAD_RELATION = 5
    const val PAYLOAD_HIDE_ICON = 6


    override fun areItemsTheSame(
        oldItem: Viewer.ListView.Item,
        newItem: Viewer.ListView.Item
    ): Boolean = newItem.objectId == oldItem.objectId

    override fun areContentsTheSame(
        oldItem: Viewer.ListView.Item,
        newItem: Viewer.ListView.Item
    ): Boolean = oldItem == newItem

    override fun getChangePayload(
        oldItem: Viewer.ListView.Item,
        newItem: Viewer.ListView.Item
    ): List<Int> {
        val payload = mutableListOf<Int>()
        if (oldItem is Viewer.ListView.Item.Task && newItem is Viewer.ListView.Item.Task) {
            if (oldItem.done != newItem.done) payload.add(PAYLOAD_CHECKED)
            if (oldItem.name != newItem.name) payload.add(PAYLOAD_NAME)
            if (oldItem.description != newItem.description) payload.add(PAYLOAD_DESCRIPTION)
            if (oldItem.relations != newItem.relations) payload.add(PAYLOAD_RELATION)
        }
        if (oldItem is Viewer.ListView.Item.Profile && newItem is Viewer.ListView.Item.Profile) {
            if (oldItem.name != newItem.name) payload.add(PAYLOAD_NAME)
            if (oldItem.icon != newItem.icon) payload.add(PAYLOAD_ICON)
            if (oldItem.description != newItem.description) payload.add(PAYLOAD_DESCRIPTION)
            if (oldItem.relations != newItem.relations) payload.add(PAYLOAD_RELATION)
            if (oldItem.hideIcon != newItem.hideIcon) payload.add(PAYLOAD_HIDE_ICON)
        }
        if (oldItem is Viewer.ListView.Item.Default && newItem is Viewer.ListView.Item.Default) {
            if (oldItem.name != newItem.name) payload.add(PAYLOAD_NAME)
            if (oldItem.icon != newItem.icon) payload.add(PAYLOAD_ICON)
            if (oldItem.description != newItem.description) payload.add(PAYLOAD_DESCRIPTION)
            if (oldItem.relations != newItem.relations) payload.add(PAYLOAD_RELATION)
            if (oldItem.hideIcon != newItem.hideIcon) payload.add(PAYLOAD_HIDE_ICON)
        }
        return payload
    }
}

