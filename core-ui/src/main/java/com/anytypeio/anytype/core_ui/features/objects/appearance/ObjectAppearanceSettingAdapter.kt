package com.anytypeio.anytype.core_ui.features.objects.appearance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemObjectPreviewCoverBinding
import com.anytypeio.anytype.core_ui.databinding.ItemObjectPreviewRelationNameBinding
import com.anytypeio.anytype.core_ui.databinding.ItemObjectPreviewRelationToggleBinding
import com.anytypeio.anytype.core_ui.databinding.ItemObjectPreviewSectionBinding
import com.anytypeio.anytype.core_ui.databinding.ItemObjectPreviewSettingBinding
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView.Appearance.MenuItem
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceMainSettingsView
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceMainSettingsView.Cover
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceMainSettingsView.FeaturedRelationsSection
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceMainSettingsView.Icon
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceMainSettingsView.PreviewLayout
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceMainSettingsView.Relation
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceMainSettingsView.Relation.Description
import com.anytypeio.anytype.presentation.objects.appearance.ObjectAppearanceMainSettingsView.Toggle

class ObjectAppearanceSettingAdapter(
    private val onItemClick: (ObjectAppearanceMainSettingsView) -> Unit,
    private val onSettingToggleChanged: (Toggle, Boolean) -> Unit,
    private val onCoverToggleChanged: (Boolean) -> Unit
) : RecyclerView.Adapter<ObjectAppearanceSettingAdapter.ViewHolder>() {

    private val items: MutableList<ObjectAppearanceMainSettingsView> = mutableListOf()

    override fun getItemCount(): Int = items.size

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
            TYPE_ITEM_RELATION_TOGGLE ->
                ViewHolder.Relation.Toggle(
                    binding = ItemObjectPreviewRelationToggleBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    binding.relSwitch.setOnCheckedChangeListener { _, isChecked ->
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onSettingToggleChanged(
                                getItem(pos) as Toggle,
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
            TYPE_ITEM_SETTING_LIST -> ViewHolder.List(
                ItemObjectPreviewSettingBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            ).apply {
                itemView.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onItemClick(getItem(pos))
                    }
                }
            }
            TYPE_ITEM_COVER ->
                ViewHolder.Cover(
                    binding = ItemObjectPreviewCoverBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    binding.coverSwitch.setOnCheckedChangeListener { _, isChecked ->
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onCoverToggleChanged(isChecked)
                        }
                    }
                }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    private fun getItem(pos: Int): ObjectAppearanceMainSettingsView {
        return items[pos]
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.Relation.Name -> {}
            is ViewHolder.Section -> {}
            is ViewHolder.Relation.Toggle -> holder.bind(getItem(position) as Toggle)
            is ViewHolder.List -> holder.bind(getItem(position) as ObjectAppearanceMainSettingsView.List)
            is ViewHolder.Cover -> holder.bind(getItem(position) as Cover)
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        FeaturedRelationsSection -> TYPE_ITEM_SECTION
        is Description,
        is Icon,
        is PreviewLayout -> TYPE_ITEM_SETTING_LIST
        is Cover -> TYPE_ITEM_COVER
        is Relation.Name -> TYPE_ITEM_RELATION_NAME
        is Relation.ObjectType -> TYPE_ITEM_RELATION_TOGGLE
    }

    fun submitList(items: List<ObjectAppearanceMainSettingsView>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class List(val binding: ItemObjectPreviewSettingBinding) : ViewHolder(binding.root) {
            fun bind(item: ObjectAppearanceMainSettingsView.List) {
                with(binding) {
                    settingName.setText(getName(item))
                    settingValue.setText(getValue(item))
                }
            }

            @StringRes
            private fun getValue(item: ObjectAppearanceMainSettingsView.List): Int {
                return when (item) {
                    is Description -> when (item.description) {
                        MenuItem.Description.ADDED -> R.string.object_description
                        MenuItem.Description.CONTENT -> R.string.description_content
                        MenuItem.Description.NONE -> R.string.description_none
                    }.also { binding.relIcon.visible() }
                    is Icon -> {
                        when (item.icon) {
                            MenuItem.Icon.NONE -> R.string.none
                            MenuItem.Icon.SMALL -> R.string.small
                            MenuItem.Icon.MEDIUM -> R.string.medium
                        }
                    }
                    is PreviewLayout -> {
                        when (item.previewLayoutState) {
                            MenuItem.PreviewLayout.TEXT -> R.string.text
                            MenuItem.PreviewLayout.CARD -> R.string.card
                            MenuItem.PreviewLayout.INLINE -> R.string.inline
                        }
                    }
                }
            }

            @StringRes
            private fun getName(item: ObjectAppearanceMainSettingsView.List): Int {
                return when (item) {
                    is Description -> R.string.description
                    is Icon -> R.string.icon
                    is PreviewLayout -> R.string.preview_layout
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


            class Toggle(
                val binding: ItemObjectPreviewRelationToggleBinding
            ) : Relation(binding.root) {

                fun bind(item: ObjectAppearanceMainSettingsView.Toggle) {
                    val context = itemView.context
                    with(binding) {
                        when (item) {
                            is ObjectAppearanceMainSettingsView.Relation.ObjectType -> {
                                relIcon.setImageDrawable(context.drawable(R.drawable.ic_relation_type))
                                relName.setText(R.string.object_type)
                            }
                        }
                        relSwitch.isChecked = item.checked
                    }
                }

            }
        }

        class Cover(
            val binding: ItemObjectPreviewCoverBinding
        ) : ViewHolder(binding.root) {

            fun bind(item: ObjectAppearanceMainSettingsView.Cover) {
                when (item.coverState) {
                    MenuItem.Cover.WITH -> binding.coverSwitch.isChecked = true
                    MenuItem.Cover.WITHOUT -> binding.coverSwitch.isChecked = false
                }
            }
        }
    }

    companion object {
        private const val TYPE_ITEM_SECTION = 1
        private const val TYPE_ITEM_SETTING_LIST = 2
        private const val TYPE_ITEM_RELATION_NAME = 5
        private const val TYPE_ITEM_RELATION_TOGGLE = 6
        private const val TYPE_ITEM_COVER = 7
    }
}