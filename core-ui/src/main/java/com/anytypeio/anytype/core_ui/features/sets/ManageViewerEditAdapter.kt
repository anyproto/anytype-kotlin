package com.anytypeio.anytype.core_ui.features.sets

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.anytypeio.anytype.core_ui.common.AbstractAdapter
import com.anytypeio.anytype.core_ui.common.AbstractViewHolder
import com.anytypeio.anytype.core_ui.databinding.ItemDvManageViewerBinding
import com.anytypeio.anytype.core_ui.databinding.ItemDvManageViewerDoneBinding
import com.anytypeio.anytype.core_ui.tools.SupportDragAndDropBehavior
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.shift
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.ItemTouchHelperViewHolder
import com.anytypeio.anytype.core_utils.ui.OnStartDragListener
import com.anytypeio.anytype.presentation.sets.ManageViewerViewModel.ViewerView

class ManageViewerEditAdapter(
    private val onDragListener: OnStartDragListener,
    private val onButtonMoreClicked: (ViewerView) -> Unit
) : AbstractAdapter<ViewerView>(emptyList()), SupportDragAndDropBehavior {

    val order: List<String> get() = items.map { it.id }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(
        parent: ViewGroup,
        iewType: Int
    ): ViewHolder = ViewHolder(
        binding = ItemDvManageViewerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ).apply {
        //ToDo temporary blocked, because of missing middleware command
//        itemView.dndDragger.setOnTouchListener { _, event ->
//            if (event.action == MotionEvent.ACTION_DOWN) onDragListener.onStartDrag(this)
//            false
//        }

        binding.dndDragger.setOnClickListener {
            itemView.context.toast("Coming soon...")
        }
        binding.btnActionMore.setOnClickListener {
            onButtonMoreClicked(items[bindingAdapterPosition])
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val update = ArrayList(items).shift(fromPosition, toPosition)
        items = update
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    class ViewHolder(
        val binding: ItemDvManageViewerBinding
    ) : AbstractViewHolder<ViewerView>(binding.root), ItemTouchHelperViewHolder {

        override fun bind(item: ViewerView) {
            binding.title.text = item.name
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
    ): ViewHolder = ViewHolder(
        binding = ItemDvManageViewerDoneBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ).apply {
        itemView.setOnClickListener {
            onViewerClicked(items[bindingAdapterPosition])
        }
    }

    class ViewHolder(
        val binding: ItemDvManageViewerDoneBinding
    ) : AbstractViewHolder<ViewerView>(binding.root) {

        private val title = binding.title
        private val icChecked = binding.iconChecked

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