package com.anytypeio.anytype.core_ui.features.sets

import android.view.View
import android.view.ViewGroup
import com.anytypeio.anytype.core_models.Block.Content.DataView.Viewer
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.AbstractAdapter
import com.anytypeio.anytype.core_ui.common.AbstractViewHolder
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.sets.ManageViewerViewModel.ViewerView
import kotlinx.android.synthetic.main.item_dv_manage_viewer.view.*
import timber.log.Timber

class ManageViewerAdapter(
    private val onViewerClicked: (ViewerView) -> Unit,
    private val onViewerActionClicked: (ViewerView) -> Unit
) : AbstractAdapter<ViewerView>(emptyList()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        iewType: Int
    ): ViewHolder = ViewHolder(view = inflate(parent, R.layout.item_dv_manage_viewer)).apply {
        itemView.setOnClickListener {
            onViewerClicked(items[bindingAdapterPosition])
        }
        itemView.btnActionMore.setOnClickListener {
            onViewerActionClicked(items[bindingAdapterPosition])
        }
    }

    class ViewHolder(view: View) : AbstractViewHolder<ViewerView>(view) {

        private val icon = itemView.viewerTypeIcon
        private val menu = itemView.btnActionMore
        private val dragger = itemView.dndDragger

        override fun bind(item: ViewerView) {
            Timber.d("Binding item: $item")
            itemView.title.text = item.name
            itemView.isSelected = item.isActive
            when (item.type) {
                Viewer.Type.GRID -> icon.setBackgroundResource(R.drawable.ic_manage_dv_viewer_grid)
                Viewer.Type.LIST -> icon.setBackgroundResource(R.drawable.ic_manage_dv_viewer_list)
                Viewer.Type.GALLERY -> icon.setBackgroundResource(R.drawable.ic_manage_dv_viewer_gallery)
                Viewer.Type.BOARD -> icon.setBackgroundResource(R.drawable.ic_manage_dv_viewer_kanban)
            }
            if (item.showActionMenu) {
                menu.visible()
                dragger.visible()
            } else {
                menu.gone()
                dragger.gone()
            }
        }
    }
}