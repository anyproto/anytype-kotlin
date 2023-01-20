package com.anytypeio.anytype.core_ui.features.dataview

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType.Layout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemViewerGridRowBinding
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.sets.model.CellView
import com.anytypeio.anytype.presentation.sets.model.Viewer

class ViewerGridAdapter(
    private val onCellClicked: (CellView) -> Unit,
    private val onObjectHeaderClicked: (Id) -> Unit,
    private val onTaskCheckboxClicked: (Id) -> Unit
) : ListAdapter<Viewer.GridView.Row, ViewerGridAdapter.RecordHolder>(GridDiffUtil) {

    var recordNamePositionX = 0f

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecordHolder {

        val inflater = LayoutInflater.from(parent.context)

        val binding = ItemViewerGridRowBinding.inflate(
            inflater, parent, false
        )

        with(parent.context.resources) {
            val headerMargin = getDimensionPixelSize(R.dimen.dv_grid_name_margin_end)
            binding.headerContainer.updateLayoutParams<LinearLayout.LayoutParams> {
                width = displayMetrics.widthPixels - headerMargin * 2
            }
        }

        val horizontalDivider = binding.root.context.drawable(R.drawable.divider_dv_horizontal_2)

        binding.rowCellRecycler.apply {
            adapter = ViewerGridCellsAdapter(
                onCellClicked = onCellClicked
            )
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.HORIZONTAL
                ).apply {
                    setDrawable(horizontalDivider)
                }
            )
        }
        return RecordHolder(binding).apply {
            binding.headerContainer.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val item = getItem(pos)
                    onObjectHeaderClicked(item.id)
                }
            }
            binding.objectIcon.checkbox.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val item = getItem(pos)
                    onTaskCheckboxClicked(item.id)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecordHolder, position: Int) {
        val item = getItem(position)
        holder.bindObjectHeader(item)
        holder.bindObjectCells(item)
    }

    override fun onBindViewHolder(holder: RecordHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) super.onBindViewHolder(holder, position, payloads)
        val item = getItem(position)
        payloads.forEach { payload ->
            if (payload is List<*>) {
                if (payload.contains(GridDiffUtil.OBJECT_HEADER_CHANGED)) {
                    holder.bindObjectHeader(item)
                }
                holder.bindObjectCells(item)
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecordHolder) {
        super.onViewAttachedToWindow(holder)
        holder.binding.headerContainer.translationX = recordNamePositionX
    }

    fun clear() {
        recordNamePositionX = 0f
    }

    class RecordHolder(val binding: ItemViewerGridRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val root: LinearLayout = binding.holderRoot
        val adapter get() = binding.rowCellRecycler.adapter as ViewerGridCellsAdapter

        fun bindObjectHeader(row: Viewer.GridView.Row) {
            if (row.showIcon) {
                showIcon(row)
            } else {
                hideIcon()
            }
            binding.tvTitle.text = row.name
        }

        private fun hideIcon() {
            binding.objectIcon.gone()
            binding.objectIcon.setIcon(ObjectIcon.None)
        }

        private fun showIcon(row: Viewer.GridView.Row) {
            binding.objectIcon.visible()
            when (row.layout) {
                Layout.TODO -> binding.objectIcon.setCheckbox(row.isChecked)
                Layout.BASIC, Layout.BOOKMARK, Layout.SET -> {
                    if ((row.image != null || row.emoji != null)) {
                        if (row.image != null) {
                            binding.objectIcon.setRectangularImage(row.image)
                        } else if (row.emoji != null) {
                            binding.objectIcon.setEmoji(row.emoji)
                        }
                    } else {
                        binding.objectIcon.gone()
                    }
                }
                Layout.PROFILE -> {
                    if (row.image != null) {
                        binding.objectIcon.setCircularImage(row.image)
                    } else {
                        binding.objectIcon.setProfileInitials(row.name.orEmpty())
                    }
                }
                else -> binding.objectIcon.gone()
            }
        }

        fun bindObjectCells(row: Viewer.GridView.Row) {
            adapter.update(row.cells)
        }
    }

    object GridDiffUtil : DiffUtil.ItemCallback<Viewer.GridView.Row>() {
        override fun areItemsTheSame(
            oldItem: Viewer.GridView.Row,
            newItem: Viewer.GridView.Row
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Viewer.GridView.Row,
            newItem: Viewer.GridView.Row
        ): Boolean = oldItem == newItem

        override fun getChangePayload(
            oldItem: Viewer.GridView.Row,
            newItem: Viewer.GridView.Row
        ): List<Int>? {
            val payload = mutableListOf<Int>()
            if (isHeaderChanged(oldItem, newItem)) payload.add(OBJECT_HEADER_CHANGED)
            return if (payload.isEmpty()) {
                null
            } else payload
        }

        private fun isHeaderChanged(
            oldItem: Viewer.GridView.Row,
            newItem: Viewer.GridView.Row
        ) = (oldItem.emoji != newItem.emoji
                || oldItem.image != newItem.image
                || oldItem.name != newItem.name
                || oldItem.isChecked != newItem.isChecked
                || oldItem.layout != newItem.layout
                || oldItem.showIcon != newItem.showIcon)

        const val OBJECT_HEADER_CHANGED = 0
    }
}