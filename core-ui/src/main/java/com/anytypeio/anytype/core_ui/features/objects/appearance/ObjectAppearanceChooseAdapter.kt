package com.anytypeio.anytype.core_ui.features.objects.appearance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemObjectAppearanceCheckboxBinding
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChooseSettingsView
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChooseSettingsView.Cover
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChooseSettingsView.Description
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChooseSettingsView.Icon
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChooseSettingsView.PreviewLayout

class ObjectAppearanceChooseAdapter<T : ObjectAppearanceChooseSettingsView>(
    private val onItemClick: (T) -> Unit,
) : RecyclerView.Adapter<ObjectAppearanceChooseAdapter.ViewHolder>() {

    private val items = mutableListOf<T>()

    override fun getItemCount(): Int = items.size

    private fun getItem(position: Int): T = items[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemObjectAppearanceCheckboxBinding.inflate(
            inflater, parent, false
        )
        return when (viewType) {
            TYPE_ITEM_ICON -> ViewHolder.Icon(
                binding = binding
            )
            TYPE_ITEM_COVER -> ViewHolder.Cover(
                binding = binding
            )
            TYPE_ITEM_PREVIEW_LAYOUT -> ViewHolder.PreviewLayout(
                binding = binding
            )
            TYPE_ITEM_DESCRIPTION -> ViewHolder.Description(
                binding = binding
            )
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }.apply {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(pos))
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.Icon -> holder.bind(getItem(position) as Icon)
            is ViewHolder.Cover -> holder.bind(getItem(position) as Cover)
            is ViewHolder.PreviewLayout -> holder.bind(getItem(position) as PreviewLayout)
            is ViewHolder.Description -> holder.bind(getItem(position) as Description)
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position) as ObjectAppearanceChooseSettingsView) {
            is Cover -> TYPE_ITEM_COVER
            is Icon -> TYPE_ITEM_ICON
            is PreviewLayout -> TYPE_ITEM_PREVIEW_LAYOUT
            is Description -> TYPE_ITEM_DESCRIPTION
        }

    fun submitList(items: List<T>) {
        this.items.clear()
        this.items.addAll(items)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class Icon(val binding: ItemObjectAppearanceCheckboxBinding) : ViewHolder(binding.root) {

            fun bind(item: ObjectAppearanceChooseSettingsView.Icon) = with(binding) {
                when (item) {
                    is ObjectAppearanceChooseSettingsView.Icon.Medium -> name.setText(R.string.medium)
                    is ObjectAppearanceChooseSettingsView.Icon.Small -> name.setText(R.string.small)
                    is ObjectAppearanceChooseSettingsView.Icon.None -> name.setText(R.string.none)
                }
                if (item.isSelected) checkbox.visible() else checkbox.invisible()
                icon.gone()
            }
        }

        class Cover(val binding: ItemObjectAppearanceCheckboxBinding) : ViewHolder(binding.root) {

            fun bind(item: ObjectAppearanceChooseSettingsView.Cover) = with(binding) {
                when (item) {
                    is ObjectAppearanceChooseSettingsView.Cover.None -> name.setText(R.string.none)
                    is ObjectAppearanceChooseSettingsView.Cover.Visible -> name.setText(R.string.visible)
                }
                if (item.isSelected) checkbox.visible() else checkbox.invisible()
                icon.gone()
            }
        }

        class PreviewLayout(
            val binding: ItemObjectAppearanceCheckboxBinding
        ) : ViewHolder(binding.root) {
            fun bind(item: ObjectAppearanceChooseSettingsView.PreviewLayout) = with(binding) {
                when (item) {
                    is ObjectAppearanceChooseSettingsView.PreviewLayout.Text -> {
                        name.setText(R.string.text)
                        icon.setImageResource(R.drawable.ic_preview_layout_text)

                    }
                    is ObjectAppearanceChooseSettingsView.PreviewLayout.Card -> {
                        name.setText(R.string.card)
                        icon.setImageResource(R.drawable.ic_preview_layout_card)
                    }
                }
                if (item.isSelected) checkbox.visible() else checkbox.invisible()
            }
        }

        class Description(
            val binding: ItemObjectAppearanceCheckboxBinding
        ) : ViewHolder(binding.root) {
            fun bind(item: ObjectAppearanceChooseSettingsView.Description) = with(binding) {
                when (item) {
                    is ObjectAppearanceChooseSettingsView.Description.None -> name.setText(R.string.description_none)
                    is ObjectAppearanceChooseSettingsView.Description.Added -> name.setText(R.string.object_description)
                    is ObjectAppearanceChooseSettingsView.Description.Content -> name.setText(R.string.description_content)
                }
                icon.gone()
                if (item.isSelected) checkbox.visible() else checkbox.invisible()
            }
        }
    }

    companion object {
        private const val TYPE_ITEM_ICON = 0
        private const val TYPE_ITEM_COVER = 1
        private const val TYPE_ITEM_PREVIEW_LAYOUT = 2
        private const val TYPE_ITEM_DESCRIPTION = 3
    }
}