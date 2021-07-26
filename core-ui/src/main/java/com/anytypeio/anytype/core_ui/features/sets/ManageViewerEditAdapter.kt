package com.anytypeio.anytype.core_ui.features.sets

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.AbstractAdapter
import com.anytypeio.anytype.core_ui.common.AbstractViewHolder
import com.anytypeio.anytype.core_ui.tools.SupportDragAndDropBehavior
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.shift
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.ItemTouchHelperViewHolder
import com.anytypeio.anytype.core_utils.ui.OnStartDragListener
import com.anytypeio.anytype.presentation.sets.ManageViewerViewModel.ViewerView
import kotlinx.android.synthetic.main.item_dv_manage_viewer.view.*
import kotlinx.android.synthetic.main.item_dv_manage_viewer.view.title
import kotlinx.android.synthetic.main.item_dv_manage_viewer_done.view.*

class ManageViewerEditAdapter(
    private val onDragListener: OnStartDragListener,
    private val onButtonMoreClicked: (ViewerView) -> Unit
) : AbstractAdapter<ViewerView>(emptyList()), SupportDragAndDropBehavior {

    val order: List<String> get() = items.map { it.id }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(
        parent: ViewGroup,
        iewType: Int
    ): ViewHolder = ViewHolder(view = inflate(parent, R.layout.item_dv_manage_viewer)).apply {
        //ToDo temporary blocked, because of missing middleware command
//        itemView.dndDragger.setOnTouchListener { _, event ->
//            if (event.action == MotionEvent.ACTION_DOWN) onDragListener.onStartDrag(this)
//            false
//        }

        itemView.dndDragger.setOnClickListener {
            itemView.context.toast("Coming soon...")
        }
        itemView.btnActionMore.setOnClickListener {
            onButtonMoreClicked(items[bindingAdapterPosition])
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val update = ArrayList(items).shift(fromPosition, toPosition)
        items = update
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    class ViewHolder(view: View) : AbstractViewHolder<ViewerView>(view), ItemTouchHelperViewHolder {

        override fun bind(item: ViewerView) {
            itemView.title.text = item.name
        }

        override fun onItemSelected() {
        }

        override fun onItemClear() {
        }
    }
}

class ManageViewerDoneAdapter(
    private val onViewerClicked: (ViewerView) -> Unit
) : AbstractAdapter<ViewerView>(emptyList()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        iewType: Int
    ): ViewHolder = ViewHolder(view = inflate(parent, R.layout.item_dv_manage_viewer_done)).apply {
        itemView.setOnClickListener {
            onViewerClicked(items[bindingAdapterPosition])
        }
    }

    class ViewHolder(view: View) : AbstractViewHolder<ViewerView>(view) {

        private val title = itemView.title
        private val icChecked = itemView.iconChecked

        override fun bind(item: ViewerView) {
            title.text = item.name
            if (item.isActive) {
                icChecked.visible()
            } else {
                icChecked.invisible()
            }
        }
    }
}