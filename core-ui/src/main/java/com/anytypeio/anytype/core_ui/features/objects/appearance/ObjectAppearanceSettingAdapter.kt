package com.anytypeio.anytype.core_ui.features.objects.appearance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemObjectPreviewRelationDescriptionBinding
import com.anytypeio.anytype.core_ui.databinding.ItemObjectPreviewRelationNameBinding
import com.anytypeio.anytype.core_ui.databinding.ItemObjectPreviewSectionBinding
import com.anytypeio.anytype.core_ui.databinding.ItemObjectPreviewSettingBinding
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.MenuItem
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceMainSettingsView

class ObjectAppearanceSettingAdapter(
    private val onItemClick: (ObjectAppearanceMainSettingsView) -> Unit,
    private val onSettingToggleChanged: (ObjectAppearanceMainSettingsView.Toggle, Boolean) -> Unit
) : ListAdapter<ObjectAppearanceMainSettingsView, ObjectAppearanceSettingAdapter.ViewHolder>(
    ObjectPreviewDiffer
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_ITEM_RELATION_NAME -> ViewHolder.Relation.Name(
                ItemObjectPreviewRelationNameBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )
            TYPE_ITEM_RELATION_DESCRIPTION ->
                ViewHolder.Relation.Description(
                    binding = ItemObjectPreviewRelationDescriptionBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    binding.relSwitch.setOnCheckedChangeListener { _, isChecked ->
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onSettingToggleChanged(
                                getItem(pos) as ObjectAppearanceMainSettingsView.Toggle,
                                isChecked
                            )
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
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.Relation.Name -> {}
            is ViewHolder.Relation.Description -> {
                holder.bind(getItem(position) as ObjectAppearanceMainSettingsView.Relation.Description)
            }
            is ViewHolder.Section -> {}
            is ViewHolder.Setting.Cover -> {
                holder.bind(getItem(position) as ObjectAppearanceMainSettingsView.Cover)
            }
            is ViewHolder.Setting.Icon -> {
                holder.bind(getItem(position) as ObjectAppearanceMainSettingsView.Icon)
            }
            is ViewHolder.Setting.PreviewLayout -> {
                holder.bind(getItem(position) as ObjectAppearanceMainSettingsView.PreviewLayout)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        ObjectAppearanceMainSettingsView.FeaturedRelationsSection -> TYPE_ITEM_SECTION
        is ObjectAppearanceMainSettingsView.Cover -> TYPE_ITEM_SETTING_COVER
        is ObjectAppearanceMainSettingsView.Icon -> TYPE_ITEM_SETTING_ICON
        is ObjectAppearanceMainSettingsView.PreviewLayout -> TYPE_ITEM_SETTING_PREVIEW_LAYOUT
        is ObjectAppearanceMainSettingsView.Relation.Description -> TYPE_ITEM_RELATION_DESCRIPTION
        is ObjectAppearanceMainSettingsView.Relation.Name -> TYPE_ITEM_RELATION_NAME
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        sealed class Setting(val binding: ItemObjectPreviewSettingBinding) :
            ViewHolder(binding.root) {
            class PreviewLayout(parent: ViewGroup) : Setting(
                ItemObjectPreviewSettingBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            ) {

                fun bind(item: ObjectAppearanceMainSettingsView.PreviewLayout) =
                    with(binding) {
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

                fun bind(item: ObjectAppearanceMainSettingsView.Icon) = with(binding) {
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

                fun bind(item: ObjectAppearanceMainSettingsView.Cover) = with(binding) {
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
        ) : ViewHolder(binding.root)

        sealed class Relation(root: View) : ViewHolder(root) {

            class Name(
                binding: ItemObjectPreviewRelationNameBinding,
            ) : Relation(binding.root)


            class Description(
                val binding: ItemObjectPreviewRelationDescriptionBinding
            ) : Relation(binding.root) {

                fun bind(item: ObjectAppearanceMainSettingsView.Relation.Description) =
                    with(binding) {
                        relSwitch.isChecked = when (item.description) {
                            MenuItem.Description.WITH -> true
                            MenuItem.Description.WITHOUT -> false
                        }
                    }
            }
        }
    }

    companion object {
        private const val TYPE_ITEM_SECTION = 1
        private const val TYPE_ITEM_SETTING_COVER = 2
        private const val TYPE_ITEM_SETTING_PREVIEW_LAYOUT = 3
        private const val TYPE_ITEM_SETTING_ICON = 4
        private const val TYPE_ITEM_RELATION_NAME = 5
        private const val TYPE_ITEM_RELATION_DESCRIPTION = 6
    }

    object ObjectPreviewDiffer : DiffUtil.ItemCallback<ObjectAppearanceMainSettingsView>() {

        override fun areItemsTheSame(
            oldItem: ObjectAppearanceMainSettingsView,
            newItem: ObjectAppearanceMainSettingsView
        ): Boolean = oldItem == newItem

        override fun areContentsTheSame(
            oldItem: ObjectAppearanceMainSettingsView,
            newItem: ObjectAppearanceMainSettingsView
        ): Boolean = oldItem == newItem
    }
}