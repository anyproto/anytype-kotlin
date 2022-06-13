package com.anytypeio.anytype.core_ui.features.objects.appearance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemObjectAppearanceCheckboxBinding
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChooseSettingsView
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChooseSettingsView.Cover
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChooseSettingsView.Icon
import com.anytypeio.anytype.presentation.objects.appearance.choose.ObjectAppearanceChooseSettingsView.PreviewLayout

class ObjectAppearanceChooseAdapter<T : ObjectAppearanceChooseSettingsView>(
    private val onItemClick: (T) -> Unit,
) : ListAdapter<T, ObjectAppearanceChooseAdapter.ViewHolder>(
    ObjectPreviewDiffer<T>()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
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
            is ViewHolder.Icon -> {
                holder.bind(getItem(position) as Icon)
            }
            is ViewHolder.Cover -> {
                holder.bind(getItem(position) as Cover)
            }
            is ViewHolder.PreviewLayout -> {
                holder.bind(getItem(position) as PreviewLayout)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (val item = getItem(position)) {
        is Cover -> TYPE_ITEM_COVER
        is Icon -> TYPE_ITEM_ICON
        is PreviewLayout -> TYPE_ITEM_PREVIEW_LAYOUT
        else -> throw IllegalStateException("Can't return viewType for $item")
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class Icon(val binding: ItemObjectAppearanceCheckboxBinding) : ViewHolder(binding.root) {

            fun bind(item: ObjectAppearanceChooseSettingsView.Icon) = with(binding) {
                when (item) {
                    is ObjectAppearanceChooseSettingsView.Icon.Medium -> {
                        tvSize.text = itemView.context.getString(R.string.medium)
                        ivIcon.gone()
                        if (item.isSelected) ivCheckbox.visible() else ivCheckbox.invisible()
                    }
                    is ObjectAppearanceChooseSettingsView.Icon.Small -> {
                        tvSize.text = itemView.context.getString(R.string.small)
                        ivIcon.gone()
                        if (item.isSelected) ivCheckbox.visible() else ivCheckbox.invisible()
                    }
                    is ObjectAppearanceChooseSettingsView.Icon.None -> {
                        tvSize.text = itemView.context.getString(R.string.none)
                        ivIcon.gone()
                        if (item.isSelected) ivCheckbox.visible() else ivCheckbox.invisible()
                    }
                }
            }
        }

        class Cover(val binding: ItemObjectAppearanceCheckboxBinding) : ViewHolder(binding.root) {

            fun bind(item: ObjectAppearanceChooseSettingsView.Cover) = with(binding) {
                when (item) {
                    is ObjectAppearanceChooseSettingsView.Cover.None -> {
                        tvSize.text = itemView.context.getString(R.string.none)
                        ivIcon.gone()
                        if (item.isSelected) ivCheckbox.visible() else ivCheckbox.invisible()
                    }
                    is ObjectAppearanceChooseSettingsView.Cover.Visible -> {
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
            fun bind(item: ObjectAppearanceChooseSettingsView.PreviewLayout) = with(binding) {
                when (item) {
                    is ObjectAppearanceChooseSettingsView.PreviewLayout.Text -> {
                        tvSize.text = itemView.context.getString(R.string.text)
                        ivIcon.setImageResource(R.drawable.ic_preview_layout_text)
                        if (item.isSelected) ivCheckbox.visible() else ivCheckbox.invisible()
                    }
                    is ObjectAppearanceChooseSettingsView.PreviewLayout.Card -> {
                        tvSize.text = itemView.context.getString(R.string.card)
                        ivIcon.setImageResource(R.drawable.ic_preview_layout_card)
                        if (item.isSelected) ivCheckbox.visible() else ivCheckbox.invisible()
                    }
                }
            }
        }
    }

    companion object {
        private const val TYPE_ITEM_ICON = 7
        private const val TYPE_ITEM_COVER = 8
        private const val TYPE_ITEM_PREVIEW_LAYOUT = 9
    }


    private class ObjectPreviewDiffer<T : ObjectAppearanceChooseSettingsView> :
        DiffUtil.ItemCallback<T>() {

        override fun areItemsTheSame(
            oldItem: T,
            newItem: T
        ): Boolean = oldItem == newItem

        override fun areContentsTheSame(
            oldItem: T,
            newItem: T
        ): Boolean = oldItem == newItem
    }
}