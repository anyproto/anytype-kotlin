package com.anytypeio.anytype.core_ui.features.objects

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.Companion.LINK_ICON_SIZE_LARGE
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.Companion.LINK_ICON_SIZE_MEDIUM
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.Companion.LINK_ICON_SIZE_SMALL
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.Companion.LINK_STYLE_CARD
import com.anytypeio.anytype.presentation.objects.ObjectAppearanceSettingView
import kotlinx.android.synthetic.main.item_object_appearance_checkbox.view.*
import kotlinx.android.synthetic.main.item_object_preview_relation.view.*
import kotlinx.android.synthetic.main.item_object_preview_section.view.*
import kotlinx.android.synthetic.main.item_object_preview_setting.view.*

class ObjectAppearanceSettingAdapter(
    private val onItemClick: (ObjectAppearanceSettingView) -> Unit
) : ListAdapter<ObjectAppearanceSettingView, ObjectAppearanceSettingAdapter.ViewHolder>(
    ObjectPreviewDiffer
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            TYPE_ITEM_RELATION_NAME, TYPE_ITEM_RELATION_DESCRIPTION ->
                ViewHolder.Relation(parent).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onItemClick(getItem(pos))
                        }
                    }
                }
            TYPE_ITEM_SECTION -> ViewHolder.Section(parent)
            TYPE_ITEM_SETTING_ICON -> ViewHolder.Setting.Icon(parent).apply {
                itemView.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onItemClick(getItem(pos))
                    }
                }
            }
            TYPE_ITEM_SETTING_PREVIEW_LAYOUT -> ViewHolder.Setting.PreviewLayout(parent).apply {
                itemView.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onItemClick(getItem(pos))
                    }
                }
            }
            TYPE_ITEM_SETTING_COVER -> ViewHolder.Setting.Cover(parent).apply {
                itemView.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onItemClick(getItem(pos))
                    }
                }
            }
            TYPE_ITEM_ICON -> ViewHolder.Icon(parent).apply {

            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.Relation -> {
                holder.bind(getItem(position) as ObjectAppearanceSettingView.Relation)
            }
            is ViewHolder.Section -> {
                holder.bind(getItem(position) as ObjectAppearanceSettingView.Section)
            }
            is ViewHolder.Setting.Cover -> {
                holder.bind(getItem(position) as ObjectAppearanceSettingView.Settings.Cover)
            }
            is ViewHolder.Setting.Icon -> {
                holder.bind(getItem(position) as ObjectAppearanceSettingView.Settings.Icon)
            }
            is ViewHolder.Setting.PreviewLayout -> {
                holder.bind(getItem(position) as ObjectAppearanceSettingView.Settings.PreviewLayout)
            }
            is ViewHolder.Icon -> {
                holder.bind(getItem(position) as ObjectAppearanceSettingView.Icon)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        ObjectAppearanceSettingView.Section.FeaturedRelations -> TYPE_ITEM_SECTION
        is ObjectAppearanceSettingView.Settings.Cover -> TYPE_ITEM_SETTING_COVER
        is ObjectAppearanceSettingView.Settings.Icon -> TYPE_ITEM_SETTING_ICON
        is ObjectAppearanceSettingView.Settings.PreviewLayout -> TYPE_ITEM_SETTING_PREVIEW_LAYOUT
        is ObjectAppearanceSettingView.Relation.Description -> TYPE_ITEM_RELATION_DESCRIPTION
        is ObjectAppearanceSettingView.Relation.Name -> TYPE_ITEM_RELATION_NAME
        is ObjectAppearanceSettingView.Icon.Large -> TYPE_ITEM_ICON
        is ObjectAppearanceSettingView.Icon.Medium -> TYPE_ITEM_ICON
        is ObjectAppearanceSettingView.Icon.Small -> TYPE_ITEM_ICON
        is ObjectAppearanceSettingView.Icon.None -> TYPE_ITEM_ICON
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        sealed class Setting(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_object_preview_setting, parent, false
            )
        ) {
            class PreviewLayout(parent: ViewGroup) : Setting(parent) {

                fun bind(item: ObjectAppearanceSettingView.Settings.PreviewLayout) =
                    with(itemView) {
                        settingName.text = context.getString(R.string.preview_layout)
                        settingValue.text = if (item.style == LINK_STYLE_CARD) {
                            context.getString(R.string.card)
                        } else {
                            context.getString(R.string.text)
                        }
                    }
            }

            class Icon(parent: ViewGroup) : Setting(parent) {

                fun bind(item: ObjectAppearanceSettingView.Settings.Icon) = with(itemView) {
                    settingName.text = context.getString(R.string.icon)
                    settingValue.text = if (item.withIcon == true) {
                        when (item.size) {
                            LINK_ICON_SIZE_SMALL -> context.getString(R.string.small)
                            LINK_ICON_SIZE_MEDIUM -> context.getString(R.string.medium)
                            LINK_ICON_SIZE_LARGE -> context.getString(R.string.large)
                            else -> context.getString(R.string.none)
                        }
                    } else {
                        context.getString(R.string.none)
                    }
                }
            }

            class Cover(parent: ViewGroup) : Setting(parent) {

                fun bind(item: ObjectAppearanceSettingView.Settings.Cover) = with(itemView) {
                    settingName.text = context.getString(R.string.cover)
                    settingValue.text = if (item.withCover == true) {
                        context.getString(R.string.visible)
                    } else {
                        context.getString(R.string.none)
                    }
                }
            }
        }

        class Section(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_object_preview_section, parent, false)
        ) {

            fun bind(item: ObjectAppearanceSettingView.Section) {
                itemView.section.text = when (item) {
                    ObjectAppearanceSettingView.Section.FeaturedRelations ->
                        itemView.context.getString(R.string.relations)
                }
            }
        }

        class Relation(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_object_preview_relation, parent, false
            )
        ) {

            fun bind(item: ObjectAppearanceSettingView.Relation) = with(itemView) {
                when (item) {
                    is ObjectAppearanceSettingView.Relation.Description -> {
                        relIcon.setImageResource(R.drawable.ic_relation_description)
                        relName.text = context.getString(R.string.description)
                        relSwitch.isChecked = item.withDescription ?: false
                    }
                    is ObjectAppearanceSettingView.Relation.Name -> {
                        relIcon.setImageResource(R.drawable.ic_relation_name)
                        relName.text = context.getString(R.string.name)
                        relSwitch.isChecked = item.withName ?: false
                    }
                }
            }
        }

        class Icon(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_object_appearance_checkbox, parent, false
            )
        ) {

            fun bind(item: ObjectAppearanceSettingView.Icon) = with(itemView) {
                when (item) {
                    is ObjectAppearanceSettingView.Icon.Large -> {
                        tvSize.text = context.getString(R.string.large)
                        ivIcon.gone()
                        if (item.isSelected) ivCheckbox.visible() else ivCheckbox.invisible()
                    }
                    is ObjectAppearanceSettingView.Icon.Medium -> {
                        tvSize.text = context.getString(R.string.medium)
                        ivIcon.gone()
                        if (item.isSelected) ivCheckbox.visible() else ivCheckbox.invisible()
                    }
                    is ObjectAppearanceSettingView.Icon.Small -> {
                        tvSize.text = context.getString(R.string.small)
                        ivIcon.gone()
                        if (item.isSelected) ivCheckbox.visible() else ivCheckbox.invisible()
                    }
                    is ObjectAppearanceSettingView.Icon.None -> {
                        tvSize.text = context.getString(R.string.none)
                        ivIcon.gone()
                        if (item.isSelected) ivCheckbox.visible() else ivCheckbox.invisible()
                    }
                }
            }
        }
    }

    companion object {
        const val TYPE_ITEM_SECTION = 1
        const val TYPE_ITEM_SETTING_COVER = 2
        const val TYPE_ITEM_SETTING_PREVIEW_LAYOUT = 3
        const val TYPE_ITEM_SETTING_ICON = 4
        const val TYPE_ITEM_RELATION_NAME = 5
        const val TYPE_ITEM_RELATION_DESCRIPTION = 6
        const val TYPE_ITEM_ICON = 7
        const val TYPE_ITEM_COVER = 8
        const val TYPE_ITEM_PREVIEW_LAYOUT = 9
    }

    object ObjectPreviewDiffer : DiffUtil.ItemCallback<ObjectAppearanceSettingView>() {

        override fun areItemsTheSame(
            oldItem: ObjectAppearanceSettingView,
            newItem: ObjectAppearanceSettingView
        ): Boolean = oldItem == newItem

        override fun areContentsTheSame(
            oldItem: ObjectAppearanceSettingView,
            newItem: ObjectAppearanceSettingView
        ): Boolean = oldItem == newItem
    }
}