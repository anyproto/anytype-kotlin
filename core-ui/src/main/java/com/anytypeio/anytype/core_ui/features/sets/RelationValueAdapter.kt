package com.anytypeio.anytype.core_ui.features.sets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.getMimeIcon
import com.anytypeio.anytype.core_ui.tools.SupportDragAndDropBehavior
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.DragAndDropViewHolder
import com.anytypeio.anytype.presentation.sets.RelationValueBaseViewModel.RelationValueView
import kotlinx.android.synthetic.main.item_edit_cell_file.view.*
import kotlinx.android.synthetic.main.item_edit_cell_object.view.*
import kotlinx.android.synthetic.main.item_edit_cell_object.view.tvTitle
import kotlinx.android.synthetic.main.item_edit_cell_option_create.view.*
import kotlinx.android.synthetic.main.item_edit_cell_status.view.*
import kotlinx.android.synthetic.main.item_edit_cell_tag.view.*

class RelationValueAdapter(
    private val onTagClicked: (RelationValueView.Tag) -> Unit,
    private val onStatusClicked: (RelationValueView.Status) -> Unit,
    private val onRemoveTagClicked: (RelationValueView.Tag) -> Unit,
    private val onRemoveStatusClicked: (RelationValueView.Status) -> Unit,
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
                ViewHolder.Create(inflater.inflate(viewType, parent, false)).apply {
                    itemView.setOnClickListener {
                        val item = views[bindingAdapterPosition] as RelationValueView.Create
                        onCreateOptionClicked(item.name)
                    }
                }
            }
            R.layout.item_edit_cell_tag -> {
                ViewHolder.Tag(inflater.inflate(viewType, parent, false)).apply {
                    itemView.setOnClickListener {
                        val item = views[bindingAdapterPosition] as RelationValueView.Tag
                        if (!item.removeable) onTagClicked(item)
                    }
                    itemView.btnRemoveTag.setOnClickListener {
                        val item = views[bindingAdapterPosition] as RelationValueView.Tag
                        onRemoveTagClicked(item)
                    }
                }
            }
            R.layout.item_edit_cell_status -> {
                ViewHolder.Status(inflater.inflate(viewType, parent, false)).apply {
                    itemView.setOnClickListener {
                        val item = views[bindingAdapterPosition] as RelationValueView.Status
                        if (!item.removeable) onStatusClicked(item)
                    }
                    itemView.btnRemoveStatus.setOnClickListener {
                        val item = views[bindingAdapterPosition] as RelationValueView.Status
                        onRemoveStatusClicked(item)
                    }
                }
            }
            R.layout.item_edit_cell_object -> {
                ViewHolder.Object(inflater.inflate(viewType, parent, false)).apply {
                    itemView.setOnClickListener {
                        val item = views[bindingAdapterPosition] as RelationValueView.Object
                        if (item is RelationValueView.Object.Default && !item.removeable) {
                            onObjectClicked(item)
                        }
                    }
                    itemView.btnRemoveObject.setOnClickListener {
                        val item = views[bindingAdapterPosition] as RelationValueView.Object
                        onRemoveObjectClicked(item.id)
                    }
                }
            }
            R.layout.item_edit_cell_object_non_existent -> {
                ViewHolder.NonExistentObject(inflater.inflate(viewType, parent, false)).apply {
                    itemView.setOnClickListener {
                        val item = views[bindingAdapterPosition] as RelationValueView.Object
                        if (item is RelationValueView.Object.NonExistent && !item.removeable) {
                            onObjectClicked(item)
                        }
                    }
                    itemView.btnRemoveObject.setOnClickListener {
                        val item = views[bindingAdapterPosition] as RelationValueView.Object
                        onRemoveObjectClicked(item.id)
                    }
                }
            }
            R.layout.item_edit_cell_file -> {
                ViewHolder.File(inflater.inflate(viewType, parent, false)).apply {
                    itemView.setOnClickListener {
                        val item = views[bindingAdapterPosition] as RelationValueView.File
                        if (!item.removeable) onFileClicked(item)
                    }
                    itemView.btnRemoveFile.setOnClickListener {
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
                holder.bind(views[position] as RelationValueView.Tag)
            }
            is ViewHolder.Status -> {
                holder.bind(views[position] as RelationValueView.Status)
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
        }
    }

    override fun getItemViewType(position: Int): Int = when (views[position]) {
        is RelationValueView.Empty -> R.layout.item_edit_cell_tag_or_status_empty
        is RelationValueView.Create -> R.layout.item_edit_cell_option_create
        is RelationValueView.Tag -> R.layout.item_edit_cell_tag
        is RelationValueView.Status -> R.layout.item_edit_cell_status
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

    fun order() : List<Id> {
        return views.mapNotNull { view ->
            when(view) {
                is RelationValueView.Status -> view.id
                is RelationValueView.Tag -> view.id
                is RelationValueView.Object -> view.id
                is RelationValueView.File -> view.id
                else -> null
            }
        }
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class Empty(view: View) : ViewHolder(view)

        class Create(view: View) : ViewHolder(view) {
            fun bind(item: RelationValueView.Create) {
                itemView.tvCreateOptionValue.text = itemView.context.getString(R.string.create_option, item.name)
            }
        }

        class Tag(view: View) : ViewHolder(view), DragAndDropViewHolder {
            fun bind(item: RelationValueView.Tag): Unit = with(itemView) {
                tvTagName.setup(item.name, item.color)
                if (!item.removeable) {
                    btnRemoveTag.gone()
                    btnDragAndDropTag.gone()
                    tvTagName.updateLayoutParams<FrameLayout.LayoutParams> {
                        marginStart = 0
                    }
                } else {
                    btnRemoveTag.visible()
                    btnDragAndDropTag.visible()
                    tvTagName.updateLayoutParams<FrameLayout.LayoutParams> {
                        marginStart = itemView.context.dimen(R.dimen.edit_tag_list_text_margin_start).toInt()
                    }
                }
                item.isSelected?.let { isTagSelected ->
                    tagCheckbox.visible()
                    tagCheckbox.isSelected = isTagSelected
                } ?: run { tagCheckbox.invisible() }
            }
        }

        class Status(view: View) : ViewHolder(view) {
            fun bind(item: RelationValueView.Status) = with(itemView) {
                tvStatusName.text = item.name
                tvStatusName.setColor(item.color)
                if (!item.removeable) {
                    btnRemoveStatus.gone()
                    tvStatusName.updateLayoutParams<FrameLayout.LayoutParams> {
                        marginStart = 0
                    }
                } else {
                    btnRemoveStatus.visible()
                    tvStatusName.updateLayoutParams<FrameLayout.LayoutParams> {
                        marginStart = itemView.context.dimen(R.dimen.edit_tag_list_text_margin_start).toInt()
                    }
                }
            }
        }

        class Object(view: View) : ViewHolder(view), DragAndDropViewHolder {
            fun bind(item: RelationValueView.Object.Default) = with(itemView) {
                tvTitle.text = item.name
                if (item.typeName != null) {
                    tvSubtitle.text = item.typeName
                } else {
                    tvSubtitle.setText(R.string.unknown_object_type)
                }
                if (!item.removeable) {
                    btnRemoveObject.gone()
                    btnDragAndDropObject.gone()
                } else {
                    btnRemoveObject.visible()
                    btnDragAndDropObject.visible()
                }
                iconWidget.setIcon(item.icon)
            }
        }

        class NonExistentObject(view: View) : ViewHolder(view), DragAndDropViewHolder {
            fun bind(item: RelationValueView.Object.NonExistent) = with(itemView) {
                if (!item.removeable) {
                    btnRemoveObject.gone()
                    btnDragAndDropObject.gone()
                } else {
                    btnRemoveObject.visible()
                    btnDragAndDropObject.visible()
                }
            }
        }

        class File(view: View) : ViewHolder(view), DragAndDropViewHolder {
            fun bind(item: RelationValueView.File) = with(itemView) {
                tvTitle.text = "${item.name}.${item.ext}"
                val mimeIcon = item.mime.getMimeIcon(item.name)
                iconMime.setImageResource(mimeIcon)
                if (!item.removeable) {
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