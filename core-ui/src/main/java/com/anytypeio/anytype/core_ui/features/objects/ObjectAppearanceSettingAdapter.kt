package com.anytypeio.anytype.core_ui.features.objects

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemObjectAppearanceCheckboxBinding
import com.anytypeio.anytype.core_ui.databinding.ItemObjectPreviewRelationBinding
import com.anytypeio.anytype.core_ui.databinding.ItemObjectPreviewSectionBinding
import com.anytypeio.anytype.core_ui.databinding.ItemObjectPreviewSettingBinding
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.MenuItem
import com.anytypeio.anytype.presentation.objects.ObjectAppearanceSettingView

class ObjectAppearanceSettingAdapter(
    private val onItemClick: (ObjectAppearanceSettingView) -> Unit,
    private val onSettingToggleChanged: (ObjectAppearanceSettingView, Boolean) -> Unit
) : ListAdapter<ObjectAppearanceSettingView, ObjectAppearanceSettingAdapter.ViewHolder>(
    ObjectPreviewDiffer
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_ITEM_RELATION_NAME, TYPE_ITEM_RELATION_DESCRIPTION ->
                ViewHolder.Relation(
                    binding = ItemObjectPreviewRelationBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    binding.relSwitch.setOnCheckedChangeListener { _, isChecked ->
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onSettingToggleChanged(getItem(pos), isChecked)
                        }
                    }
                }
            TYPE_ITEM_SECTION -> ViewHolder.Section(
                binding = ItemObjectPreviewSectionBinding.inflate(
                    inflater, parent, false
                )
            )
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
            TYPE_ITEM_ICON -> ViewHolder.Icon(
                binding = ItemObjectAppearanceCheckboxBinding.inflate(
                    inflater, parent, false
                )
            ).apply {
                itemView.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onItemClick(getItem(pos))
                    }
                }
            }
            TYPE_ITEM_COVER -> ViewHolder.Cover(
                binding = ItemObjectAppearanceCheckboxBinding.inflate(
                    inflater, parent, false
                )
            ).apply {
                itemView.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onItemClick(getItem(pos))
                    }
                }
            }
            TYPE_ITEM_PREVIEW_LAYOUT -> ViewHolder.PreviewLayout(
                binding = ItemObjectAppearanceCheckboxBinding.inflate(
                    inflater, parent, false
                )
            ).apply {
                itemView.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onItemClick(getItem(pos))
                    }
                }
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
            is ViewHolder.Cover -> {
                holder.bind(getItem(position) as ObjectAppearanceSettingView.Cover)
            }
            is ViewHolder.PreviewLayout -> {
                holder.bind(getItem(position) as ObjectAppearanceSettingView.PreviewLayout)
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
        is ObjectAppearanceSettingView.Icon -> TYPE_ITEM_ICON
        is ObjectAppearanceSettingView.Cover -> TYPE_ITEM_COVER
        is ObjectAppearanceSettingView.PreviewLayout -> TYPE_ITEM_PREVIEW_LAYOUT
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        sealed class Setting(val binding: ItemObjectPreviewSettingBinding) :
            ViewHolder(binding.root) {
            class PreviewLayout(parent: ViewGroup) : Setting(
                ItemObjectPreviewSettingBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            ) {

                fun bind(item: ObjectAppearanceSettingView.Settings.PreviewLayout) = with(binding) {
                    settingName.text = itemView.context.getString(R.string.preview_layout)
                    settingValue.text = when (item.previewLayoutState) {
                        MenuItem.PreviewLayout.TEXT -> itemView.context.getString(R.string.text)
                        MenuItem.PreviewLayout.CARD -> itemView.context.getString(R.string.card)
                    }
                }
            }

            class Icon(parent: ViewGroup) : Setting(
                ItemObjectPreviewSettingBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            ) {

                fun bind(item: ObjectAppearanceSettingView.Settings.Icon) = with(binding) {
                    settingName.text = itemView.context.getString(R.string.icon)
                    settingValue.text = when (item.icon) {
                        MenuItem.Icon.NONE -> itemView.context.getString(R.string.none)
                        MenuItem.Icon.SMALL -> itemView.context.getString(R.string.small)
                        MenuItem.Icon.MEDIUM -> itemView.context.getString(R.string.medium)
                    }
                }
            }

            class Cover(parent: ViewGroup) : Setting(
                ItemObjectPreviewSettingBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            ) {

                fun bind(item: ObjectAppearanceSettingView.Settings.Cover) = with(binding) {
                    settingName.text = itemView.context.getString(R.string.cover)
                    settingValue.text = when (item.coverState) {
                        MenuItem.Cover.WITH -> itemView.context.getString(R.string.visible)
                        MenuItem.Cover.WITHOUT -> itemView.context.getString(R.string.none)
                    }
                }
            }
        }

        class Section(
            val binding: ItemObjectPreviewSectionBinding
        ) : ViewHolder(binding.root) {

            fun bind(item: ObjectAppearanceSettingView.Section) {
                binding.section.text = when (item) {
                    ObjectAppearanceSettingView.Section.FeaturedRelations -> {
                        itemView.context.getString(R.string.relations)
                    }
                }
            }
        }

        class Relation(
            val binding: ItemObjectPreviewRelationBinding
        ) : ViewHolder(binding.root) {

            fun bind(item: ObjectAppearanceSettingView.Relation) = with(binding) {
                when (item) {
                    is ObjectAppearanceSettingView.Relation.Description -> {
                        relIcon.setImageResource(R.drawable.ic_relation_description)
                        relName.text = itemView.context.getString(R.string.description)
                        relSwitch.visible()
                        relSwitch.isChecked = when (item.description) {
                            MenuItem.Description.WITH -> true
                            MenuItem.Description.WITHOUT -> false
                        }
                    }
                    is ObjectAppearanceSettingView.Relation.Name -> {
                        relIcon.setImageResource(R.drawable.ic_relation_name)
                        relName.text = itemView.context.getString(R.string.name)
                        relSwitch.gone()
                    }
                }
            }
        }

        class Icon(val binding: ItemObjectAppearanceCheckboxBinding) : ViewHolder(binding.root) {

            fun bind(item: ObjectAppearanceSettingView.Icon) = with(binding) {
                when (item) {
                    is ObjectAppearanceSettingView.Icon.Medium -> {
                        tvSize.text = itemView.context.getString(R.string.medium)
                        ivIcon.gone()
                        if (item.isSelected) ivCheckbox.visible() else ivCheckbox.invisible()
                    }
                    is ObjectAppearanceSettingView.Icon.Small -> {
                        tvSize.text = itemView.context.getString(R.string.small)
                        ivIcon.gone()
                        if (item.isSelected) ivCheckbox.visible() else ivCheckbox.invisible()
                    }
                    is ObjectAppearanceSettingView.Icon.None -> {
                        tvSize.text = itemView.context.getString(R.string.none)
                        ivIcon.gone()
                        if (item.isSelected) ivCheckbox.visible() else ivCheckbox.invisible()
                    }
                }
            }
        }

        class Cover(val binding: ItemObjectAppearanceCheckboxBinding) : ViewHolder(binding.root) {

            fun bind(item: ObjectAppearanceSettingView.Cover) = with(binding) {
                when (item) {
                    is ObjectAppearanceSettingView.Cover.None -> {
                        tvSize.text = itemView.context.getString(R.string.none)
                        ivIcon.gone()
                        if (item.isSelected) ivCheckbox.visible() else ivCheckbox.invisible()
                    }
                    is ObjectAppearanceSettingView.Cover.Visible -> {
                        tvSize.text = itemView.context.getString(R.string.visible)
                        ivIcon.gone()
                        if (item.isSelected) ivCheckbox.visible() else ivCheckbox.invisible()
                    }
                }
            }
        }

        class PreviewLayout(
            val binding: ItemObjectAppearanceCheckboxBinding
        ) : ViewHolder(binding.root) {
            fun bind(item: ObjectAppearanceSettingView.PreviewLayout) = with(binding) {
                when (item) {
                    is ObjectAppearanceSettingView.PreviewLayout.Text -> {
                        tvSize.text = itemView.context.getString(R.string.text)
                        ivIcon.setImageResource(R.drawable.ic_preview_layout_text)
                        if (item.isSelected) ivCheckbox.visible() else ivCheckbox.invisible()
                    }
                    is ObjectAppearanceSettingView.PreviewLayout.Card -> {
                        tvSize.text = itemView.context.getString(R.string.card)
                        ivIcon.setImageResource(R.drawable.ic_preview_layout_card)
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