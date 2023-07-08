package com.anytypeio.anytype.core_ui.features.sets

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.AbstractAdapter
import com.anytypeio.anytype.core_ui.common.AbstractViewHolder
import com.anytypeio.anytype.core_ui.databinding.ItemDvManageViewerBinding
import com.anytypeio.anytype.core_ui.databinding.ItemDvManageViewerDoneBinding
import com.anytypeio.anytype.core_ui.tools.SupportDragAndDropBehavior
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.shift
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.ItemTouchHelperViewHolder
import com.anytypeio.anytype.core_utils.ui.OnStartDragListener
import com.anytypeio.anytype.presentation.sets.ManageViewerViewModel.ViewerView

class ManageViewerEditAdapter(
    private val onDragListener: OnStartDragListener,
    private val onButtonMoreClicked: (ViewerView) -> Unit,
    private val onDeleteView: (ViewerView) -> Unit,
    private val onDeleteActiveView: () -> Unit
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
        binding.dndDragger.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) onDragListener.onStartDrag(this)
            false
        }
        binding.icRemove.setOnClickListener {
            val pos = bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onDeleteView(items[pos])
            }
        }
        binding.btnActionMore.setOnClickListener {
            val pos = bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onButtonMoreClicked(items[pos])
            }
        }
        binding.icRemoveInactive.setOnClickListener {
            onDeleteActiveView()
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

        val untitled = binding.root.context.getString(R.string.untitled)

        override fun bind(item: ViewerView) {
            binding.title.text = item.name.ifEmpty {
                untitled
            }
            if (item.isActive) {
                binding.icRemove.gone()
                binding.icRemoveInactive.visible()
            } else {
                binding.icRemove.visible()
                binding.icRemoveInactive.gone()
            }
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
        val untitled = binding.root.context.getString(R.string.untitled)

        override fun bind(item: ViewerView) {
            title.text = item.name.ifEmpty {
                untitled
            }
            if (item.isActive) {
                icChecked.visible()
            } else {
                icChecked.invisible()
            }
        }
    }
}