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
import com.anytypeio.anytype.core_utils.const.MimeTypes
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.DragAndDropViewHolder
import com.anytypeio.anytype.presentation.sets.ObjectRelationValueViewModel.ObjectRelationValueView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_edit_cell_file.view.*
import kotlinx.android.synthetic.main.item_edit_cell_object.view.*
import kotlinx.android.synthetic.main.item_edit_cell_object.view.tvTitle
import kotlinx.android.synthetic.main.item_edit_cell_option_create.view.*
import kotlinx.android.synthetic.main.item_edit_cell_status.view.*
import kotlinx.android.synthetic.main.item_edit_cell_tag.view.*

class ObjectRelationValueAdapter(
    private val onTagClicked: (ObjectRelationValueView.Tag) -> Unit,
    private val onStatusClicked: (ObjectRelationValueView.Status) -> Unit,
    private val onRemoveTagClicked: (ObjectRelationValueView.Tag) -> Unit,
    private val onRemoveStatusClicked: (ObjectRelationValueView.Status) -> Unit,
    private val onCreateOptionClicked: (String) -> Unit,
    private val onObjectClicked: (ObjectRelationValueView.Object) -> Unit,
    private val onRemoveObjectClicked: (Id) -> Unit,
    private val onFileClicked: (ObjectRelationValueView.File) -> Unit,
    private val onRemoveFileClicked: (Id) -> Unit,

) : RecyclerView.Adapter<ObjectRelationValueAdapter.ViewHolder>(), SupportDragAndDropBehavior {

    private var views = emptyList<ObjectRelationValueView>()

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
                        val item = views[bindingAdapterPosition] as ObjectRelationValueView.Create
                        onCreateOptionClicked(item.name)
                    }
                }
            }
            R.layout.item_edit_cell_tag -> {
                ViewHolder.Tag(inflater.inflate(viewType, parent, false)).apply {
                    itemView.setOnClickListener {
                        val item = views[bindingAdapterPosition] as ObjectRelationValueView.Tag
                        if (!item.removeable) onTagClicked(item)
                    }
                    itemView.btnRemoveTag.setOnClickListener {
                        val item = views[bindingAdapterPosition] as ObjectRelationValueView.Tag
                        onRemoveTagClicked(item)
                    }
                }
            }
            R.layout.item_edit_cell_status -> {
                ViewHolder.Status(inflater.inflate(viewType, parent, false)).apply {
                    itemView.setOnClickListener {
                        val item = views[bindingAdapterPosition] as ObjectRelationValueView.Status
                        if (!item.removeable) onStatusClicked(item)
                    }
                    itemView.btnRemoveStatus.setOnClickListener {
                        val item = views[bindingAdapterPosition] as ObjectRelationValueView.Status
                        onRemoveStatusClicked(item)
                    }
                }
            }
            R.layout.item_edit_cell_object -> {
                ViewHolder.Object(inflater.inflate(viewType, parent, false)).apply {
                    itemView.setOnClickListener {
                        val item = views[bindingAdapterPosition] as ObjectRelationValueView.Object
                        if (!item.removeable) onObjectClicked(item)
                    }
                    itemView.btnRemoveObject.setOnClickListener {
                        val item = views[bindingAdapterPosition] as ObjectRelationValueView.Object
                        onRemoveObjectClicked(item.id)
                    }
                }
            }
            R.layout.item_edit_cell_file -> {
                ViewHolder.File(inflater.inflate(viewType, parent, false)).apply {
                    itemView.setOnClickListener {
                        val item = views[bindingAdapterPosition] as ObjectRelationValueView.File
                        if (!item.removeable) onFileClicked(item)
                    }
                    itemView.btnRemoveFile.setOnClickListener {
                        val item = views[bindingAdapterPosition] as ObjectRelationValueView.File
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
                holder.bind(views[position] as ObjectRelationValueView.Tag)
            }
            is ViewHolder.Status -> {
                holder.bind(views[position] as ObjectRelationValueView.Status)
            }
            is ViewHolder.Object -> {
                holder.bind(views[position] as ObjectRelationValueView.Object)
            }
            is ViewHolder.Create -> {
                holder.bind(views[position] as ObjectRelationValueView.Create)
            }
            is ViewHolder.File -> {
                holder.bind(views[position] as ObjectRelationValueView.File)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (views[position]) {
        is ObjectRelationValueView.Empty -> R.layout.item_edit_cell_tag_or_status_empty
        is ObjectRelationValueView.Create -> R.layout.item_edit_cell_option_create
        is ObjectRelationValueView.Tag -> R.layout.item_edit_cell_tag
        is ObjectRelationValueView.Status -> R.layout.item_edit_cell_status
        is ObjectRelationValueView.Object -> R.layout.item_edit_cell_object
        is ObjectRelationValueView.File -> R.layout.item_edit_cell_file
    }

    fun update(update: List<ObjectRelationValueView>) {
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
                is ObjectRelationValueView.Status -> view.id
                is ObjectRelationValueView.Tag -> view.id
                is ObjectRelationValueView.Object -> view.id
                is ObjectRelationValueView.File -> view.id
                else -> null
            }
        }
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class Empty(view: View) : ViewHolder(view)

        class Create(view: View) : ViewHolder(view) {
            fun bind(item: ObjectRelationValueView.Create) {
                itemView.tvCreateOptionValue.text = itemView.context.getString(R.string.create_option, item.name)
            }
        }

        class Tag(view: View) : ViewHolder(view), DragAndDropViewHolder {
            fun bind(item: ObjectRelationValueView.Tag): Unit = with(itemView) {
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
            fun bind(item: ObjectRelationValueView.Status) = with(itemView) {
                tvStatusName.text = item.name
                tvStatusName.setColor(item.color)
                if (!item.removeable) {
                    btnRemoveStatus.gone()
                    btnDragAndDropStatus.gone()
                    tvStatusName.updateLayoutParams<FrameLayout.LayoutParams> {
                        marginStart = 0
                    }
                } else {
                    btnRemoveStatus.visible()
                    btnDragAndDropStatus.visible()
                    tvStatusName.updateLayoutParams<FrameLayout.LayoutParams> {
                        marginStart = itemView.context.dimen(R.dimen.edit_tag_list_text_margin_start).toInt()
                    }
                }
            }
        }

        class Object(view: View) : ViewHolder(view), DragAndDropViewHolder {
            fun bind(item: ObjectRelationValueView.Object) = with(itemView) {
                tvTitle.text = item.name
                if (item.type != null) {
                    tvSubtitle.text = item.type
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
                iconWidget.setIcon(
                    emoji = item.emoji,
                    image = item.image,
                    name = item.name
                )
            }
        }

        class File(view: View) : ViewHolder(view), DragAndDropViewHolder {
            fun bind(item: ObjectRelationValueView.File) = with(itemView) {
                tvTitle.text = "${item.name}.${item.ext}"
                iconMime.setImageResource(item.mime.getMimeIcon())
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