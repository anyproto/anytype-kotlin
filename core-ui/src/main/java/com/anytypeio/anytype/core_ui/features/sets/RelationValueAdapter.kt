package com.anytypeio.anytype.core_ui.features.sets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemEditCellFileBinding
import com.anytypeio.anytype.core_ui.databinding.ItemEditCellObjectBinding
import com.anytypeio.anytype.core_ui.databinding.ItemEditCellObjectNonExistentBinding
import com.anytypeio.anytype.core_ui.databinding.ItemEditCellOptionCreateBinding
import com.anytypeio.anytype.core_ui.databinding.ItemEditCellStatusBinding
import com.anytypeio.anytype.core_ui.databinding.ItemEditCellTagBinding
import com.anytypeio.anytype.core_ui.extensions.getMimeIcon
import com.anytypeio.anytype.core_ui.tools.SupportDragAndDropBehavior
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.shift
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.DragAndDropViewHolder
import com.anytypeio.anytype.presentation.relations.RelationValueView

class RelationValueAdapter(
    private val onTagClicked: (RelationValueView.Option.Tag) -> Unit,
    private val onStatusClicked: (RelationValueView.Option.Status) -> Unit,
    private val onRemoveTagClicked: (RelationValueView.Option.Tag) -> Unit,
    private val onRemoveStatusClicked: (RelationValueView.Option.Status) -> Unit,
    private val onCreateOptionClicked: (String) -> Unit,
    private val onObjectClicked: (RelationValueView.Object) -> Unit,
    private val onRemoveObjectClicked: (Id) -> Unit,
    private val onFileClicked: (RelationValueView.File) -> Unit,
    private val onRemoveFileClicked: (Id) -> Unit,
) : RecyclerView.Adapter<RelationValueAdapter.ViewHolder>(), SupportDragAndDropBehavior {

    private var views = emptyList<RelationValueView>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_edit_cell_tag_or_status_empty -> {
                ViewHolder.Empty(inflater.inflate(viewType, parent, false))
            }
            R.layout.item_edit_cell_option_create -> {
                ViewHolder.Create(
                    binding = ItemEditCellOptionCreateBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        val item = views[bindingAdapterPosition] as RelationValueView.Create
                        onCreateOptionClicked(item.name)
                    }
                }
            }
            R.layout.item_edit_cell_tag -> {
                ViewHolder.Tag(
                    binding = ItemEditCellTagBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        val item = views[bindingAdapterPosition] as RelationValueView.Option.Tag
                        if (!item.removable) onTagClicked(item)
                    }
                    binding.btnRemoveTag.setOnClickListener {
                        val item = views[bindingAdapterPosition] as RelationValueView.Option.Tag
                        onRemoveTagClicked(item)
                    }
                }
            }
            R.layout.item_edit_cell_status -> {
                ViewHolder.Status(
                    binding = ItemEditCellStatusBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        val item = views[bindingAdapterPosition] as RelationValueView.Option.Status
                        if (!item.removable) onStatusClicked(item)
                    }
                    binding.btnRemoveStatus.setOnClickListener {
                        val item = views[bindingAdapterPosition] as RelationValueView.Option.Status
                        onRemoveStatusClicked(item)
                    }
                }
            }
            R.layout.item_edit_cell_object -> {
                ViewHolder.Object(
                    binding = ItemEditCellObjectBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        val item = views[bindingAdapterPosition] as RelationValueView.Object
                        if (item is RelationValueView.Object.Default && !item.removable) {
                            onObjectClicked(item)
                        }
                    }
                    binding.btnRemoveObject.setOnClickListener {
                        val item = views[bindingAdapterPosition] as RelationValueView.Object
                        onRemoveObjectClicked(item.id)
                    }
                }
            }
            R.layout.item_edit_cell_object_non_existent -> {
                ViewHolder.NonExistentObject(
                    binding = ItemEditCellObjectNonExistentBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        val item = views[bindingAdapterPosition] as RelationValueView.Object
                        if (item is RelationValueView.Object.NonExistent && !item.removable) {
                            onObjectClicked(item)
                        }
                    }
                    binding.btnRemoveObject.setOnClickListener {
                        val item = views[bindingAdapterPosition] as RelationValueView.Object
                        onRemoveObjectClicked(item.id)
                    }
                }
            }
            R.layout.item_edit_cell_file -> {
                ViewHolder.File(
                    binding = ItemEditCellFileBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        val item = views[bindingAdapterPosition] as RelationValueView.File
                        if (!item.removable) onFileClicked(item)
                    }
                    binding.btnRemoveFile.setOnClickListener {
                        val item = views[bindingAdapterPosition] as RelationValueView.File
                        onRemoveFileClicked(item.id)
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.Tag -> {
                holder.bind(views[position] as RelationValueView.Option.Tag)
            }
            is ViewHolder.Status -> {
                holder.bind(views[position] as RelationValueView.Option.Status)
            }
            is ViewHolder.Object -> {
                holder.bind(views[position] as RelationValueView.Object.Default)
            }
            is ViewHolder.NonExistentObject -> {
                holder.bind(views[position] as RelationValueView.Object.NonExistent)
            }
            is ViewHolder.Create -> {
                holder.bind(views[position] as RelationValueView.Create)
            }
            is ViewHolder.File -> {
                holder.bind(views[position] as RelationValueView.File)
            }
            is ViewHolder.Empty -> {}
        }
    }

    override fun getItemViewType(position: Int): Int = when (views[position]) {
        is RelationValueView.Empty -> R.layout.item_edit_cell_tag_or_status_empty
        is RelationValueView.Create -> R.layout.item_edit_cell_option_create
        is RelationValueView.Option.Tag -> R.layout.item_edit_cell_tag
        is RelationValueView.Option.Status -> R.layout.item_edit_cell_status
        is RelationValueView.Object.Default -> R.layout.item_edit_cell_object
        is RelationValueView.Object.NonExistent -> R.layout.item_edit_cell_object_non_existent
        is RelationValueView.File -> R.layout.item_edit_cell_file
    }

    fun update(update: List<RelationValueView>) {
        views = update
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = views.size

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val update = ArrayList(views).shift(fromPosition, toPosition)
        views = update
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    fun order(): List<Id> {
        return views.mapNotNull { view ->
            when (view) {
                is RelationValueView.Option.Status -> view.id
                is RelationValueView.Option.Tag -> view.id
                is RelationValueView.Object -> view.id
                is RelationValueView.File -> view.id
                else -> null
            }
        }
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class Empty(view: View) : ViewHolder(view)

        class Create(val binding: ItemEditCellOptionCreateBinding) : ViewHolder(binding.root) {
            fun bind(item: RelationValueView.Create) {
                binding.tvCreateOptionValue.text =
                    itemView.context.getString(R.string.create_option, item.name)
            }
        }

        class Tag(val binding: ItemEditCellTagBinding) : ViewHolder(binding.root),
            DragAndDropViewHolder {
            fun bind(item: RelationValueView.Option.Tag): Unit = with(binding) {
                tvTagName.setup(item.name, item.color)
                if (!item.removable) {
                    btnRemoveTag.gone()
                    btnDragAndDropTag.gone()
                    tvTagName.updateLayoutParams<FrameLayout.LayoutParams> {
                        marginStart = 0
                    }
                } else {
                    btnRemoveTag.visible()
                    btnDragAndDropTag.visible()
                    tvTagName.updateLayoutParams<FrameLayout.LayoutParams> {
                        marginStart =
                            itemView.context.dimen(R.dimen.edit_tag_list_text_margin_start).toInt()
                    }
                }
                tagCheckbox.isSelected = item.isSelected
                if (item.isCheckboxShown) {
                    tagCheckbox.visible()
                } else {
                    tagCheckbox.invisible()
                }
            }
        }

        class Status(val binding: ItemEditCellStatusBinding) : ViewHolder(binding.root) {
            fun bind(item: RelationValueView.Option.Status) = with(binding) {
                tvStatusName.text = item.name
                tvStatusName.setColor(item.color)
                if (!item.removable) {
                    btnRemoveStatus.gone()
                    tvStatusName.updateLayoutParams<FrameLayout.LayoutParams> {
                        marginStart = 0
                    }
                } else {
                    btnRemoveStatus.visible()
                    tvStatusName.updateLayoutParams<FrameLayout.LayoutParams> {
                        marginStart =
                            itemView.context.dimen(R.dimen.edit_tag_list_text_margin_start).toInt()
                    }
                }
            }
        }

        class Object(val binding: ItemEditCellObjectBinding) : ViewHolder(binding.root),
            DragAndDropViewHolder {
            fun bind(item: RelationValueView.Object.Default) = with(binding) {
                tvTitle.text = item.name
                if (item.typeName != null) {
                    tvSubtitle.text = item.typeName
                } else {
                    tvSubtitle.setText(R.string.unknown_object_type)
                }
                if (!item.removable) {
                    btnRemoveObject.gone()
                    btnDragAndDropObject.gone()
                } else {
                    btnRemoveObject.visible()
                    btnDragAndDropObject.visible()
                }
                iconWidget.setIcon(item.icon)
            }
        }

        class NonExistentObject(
            val binding: ItemEditCellObjectNonExistentBinding
        ) : ViewHolder(binding.root), DragAndDropViewHolder {
            fun bind(item: RelationValueView.Object.NonExistent) = with(binding) {
                if (!item.removable) {
                    btnRemoveObject.gone()
                    btnDragAndDropObject.gone()
                } else {
                    btnRemoveObject.visible()
                    btnDragAndDropObject.visible()
                }
            }
        }

        class File(val binding: ItemEditCellFileBinding) : ViewHolder(binding.root),
            DragAndDropViewHolder {
            fun bind(item: RelationValueView.File) = with(binding) {
                tvTitle.text = "${item.name}.${item.ext}"
                val mimeIcon = item.mime.getMimeIcon(item.name)
                iconMime.setImageResource(mimeIcon)
                if (!item.removable) {
                    btnRemoveFile.gone()
                    btnDragAndDropFile.gone()
                } else {
                    btnRemoveFile.visible()
                    btnDragAndDropFile.visible()
                }
            }
        }
    }
}