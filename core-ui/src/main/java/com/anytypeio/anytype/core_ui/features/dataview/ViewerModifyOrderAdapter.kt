package com.anytypeio.anytype.core_ui.features.dataview

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent.ACTION_DOWN
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.tools.SupportDragAndDropBehavior
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.shift
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.ItemTouchHelperViewHolder
import com.anytypeio.anytype.core_utils.ui.OnStartDragListener
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.sets.model.ViewerRelationListView
import kotlinx.android.synthetic.main.item_modify_viewer_relation_order.view.*
import kotlinx.android.synthetic.main.item_modify_viewer_relation_order.view.iconRelation
import kotlinx.android.synthetic.main.item_modify_viewer_relation_order.view.title
import kotlinx.android.synthetic.main.item_viewer_relation_list.view.*
import kotlinx.android.synthetic.main.item_viewer_relation_list_section.view.*
import kotlinx.android.synthetic.main.item_viewer_relation_list_setting.view.*

class ViewerModifyOrderAdapter(
    private val dragListener: OnStartDragListener,
    private val onItemClick: (SimpleRelationView) -> Unit,
    private val onDeleteClick: (SimpleRelationView) -> Unit,
    private val onSettingToggleChanged: (ViewerRelationListView.Setting.Toggle, Boolean) -> Unit
) : RecyclerView.Adapter<ViewerModifyOrderAdapter.VH>(), SupportDragAndDropBehavior {

    var items: List<ViewerRelationListView> = emptyList()

    fun update(update: List<ViewerRelationListView>) {
        items = update
        notifyDataSetChanged()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return when(viewType) {
            VIEW_TYPE_RELATION -> {
                RelationViewHolder(parent).apply {
                    itemView.iconDrag.setOnTouchListener { _, event ->
                        if (event.action == ACTION_DOWN) dragListener.onStartDrag(this)
                        false
                    }
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            val item = items[pos]
                            if (item is ViewerRelationListView.Relation) {
                                onItemClick(item.view)
                            }
                        }
                    }
                    itemView.iconDelete.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            val item = items[pos]
                            if (item is ViewerRelationListView.Relation) {
                                onDeleteClick(item.view)
                            }
                        }
                    }
                }
            }
            VIEW_TYPE_SECTION -> SectionViewHolder(parent)
            VIEW_TYPE_SETTING -> SettingViewHolder(parent)
            VIEW_TYPE_SETTING_TOGGLE -> ToggleViewHolder(parent).apply {
                itemView.switchView.setOnCheckedChangeListener { _, isChecked ->
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        val item  = items[bindingAdapterPosition]
                        if (item is ViewerRelationListView.Setting.Toggle) {
                            onSettingToggleChanged(item, isChecked)
                        }
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        when(holder) {
            is RelationViewHolder -> {
                holder.bind(items[position] as ViewerRelationListView.Relation)
            }
            is SectionViewHolder -> {
                holder.bind(items[position] as ViewerRelationListView.Section)
            }
            is SettingViewHolder -> {
                holder.bind(items[position] as ViewerRelationListView.Setting)
            }
            is ToggleViewHolder -> {
                holder.bind(items[position] as ViewerRelationListView.Setting.Toggle)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when(items[position]) {
        is ViewerRelationListView.Relation -> VIEW_TYPE_RELATION
        is ViewerRelationListView.Section -> VIEW_TYPE_SECTION
        is ViewerRelationListView.Setting.Toggle -> VIEW_TYPE_SETTING_TOGGLE
        is ViewerRelationListView.Setting -> VIEW_TYPE_SETTING
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        return when(val to = items[toPosition]) {
            is ViewerRelationListView.Relation -> {
                if (to.view.key == Relations.NAME)
                    false
                else {
                    val update = ArrayList(items).shift(fromPosition, toPosition)
                    items = update
                    notifyItemMoved(fromPosition, toPosition)
                    true
                }
            }
            else -> false
        }
    }

    sealed class VH(view: View) : RecyclerView.ViewHolder(view)

    class RelationViewHolder(parent: ViewGroup) : VH(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_modify_viewer_relation_order,
            parent,
            false
        )
    ), ItemTouchHelperViewHolder {

        fun bind(item: ViewerRelationListView.Relation) {
            if (item.view.key == ObjectSetConfig.NAME_KEY) {
                itemView.iconDrag.invisible()
            } else {
                itemView.iconDrag.visible()
            }
            itemView.title.text = item.view.title
            itemView.iconRelation.bind(item.view.format)
            if (item.view.isReadonly || item.view.isDefault) {
                itemView.iconDelete.gone()
            } else {
                itemView.iconDelete.visible()
            }
        }

        override fun onItemSelected() {
            itemView.elevation = ITEM_ELEVATION
            itemView.setBackgroundResource(R.drawable.rectangle_modify_viewer_relation_order_dnd)
        }

        override fun onItemClear() {
            itemView.elevation = 0.0f
            itemView.setBackgroundResource(EMPTY_IMAGE_RES)
        }
    }

    class SectionViewHolder(parent: ViewGroup) : VH(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_viewer_relation_list_section,
            parent,
            false
        )
    ) {
        fun bind(item: ViewerRelationListView.Section) {
            when(item) {
                ViewerRelationListView.Section.Relations -> {
                    itemView.tvSectionName.setText(R.string.relations)
                }
                ViewerRelationListView.Section.Settings -> {
                    itemView.tvSectionName.setText(R.string.settings)
                }
            }
        }
    }

    class SettingViewHolder(parent: ViewGroup) : VH(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_viewer_relation_list_setting,
            parent,
            false
        )
    ) {
        fun bind(item: ViewerRelationListView.Setting) = with(itemView) {
            when(item) {
                is ViewerRelationListView.Setting.CardSize.Small -> {
                    tvSettingName.setText(R.string.card_size)
                    tvSettingValue.setText(R.string.small)
                }
                is ViewerRelationListView.Setting.CardSize.Large -> {
                    tvSettingName.setText(R.string.card_size)
                    tvSettingValue.setText(R.string.large)
                }
                is ViewerRelationListView.Setting.ImagePreview.None -> {
                    tvSettingName.setText(R.string.image_preview)
                    tvSettingValue.setText(R.string.none)
                }
                is ViewerRelationListView.Setting.ImagePreview.Cover -> {
                    tvSettingName.setText(R.string.image_preview)
                    tvSettingValue.setText(R.string.cover)
                }
                is ViewerRelationListView.Setting.ImagePreview.Custom -> {
                    tvSettingName.setText(R.string.image_preview)
                    tvSettingValue.text = item.name
                }
            }
        }
    }

    class ToggleViewHolder(parent: ViewGroup) : VH(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_viewer_relation_list_setting_toggle,
            parent,
            false
        )
    ) {
        fun bind(item: ViewerRelationListView.Setting.Toggle) = with(itemView) {
            when(item) {
                is ViewerRelationListView.Setting.Toggle.FitImage -> {
                    tvSettingName.setText(R.string.fit_image)
                    switchView.isChecked = item.toggled
                }
                is ViewerRelationListView.Setting.Toggle.HideIcon -> {
                    tvSettingName.setText(R.string.hide_icon)
                    switchView.isChecked = item.toggled
                }
            }
        }
    }

    companion object {
        const val EMPTY_IMAGE_RES = 0
        const val ITEM_ELEVATION = 40.0f

        const val VIEW_TYPE_SECTION = 0
        const val VIEW_TYPE_RELATION = 1
        const val VIEW_TYPE_SETTING = 2
        const val VIEW_TYPE_SETTING_TOGGLE = 3
    }
}