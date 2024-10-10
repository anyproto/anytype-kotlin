package com.anytypeio.anytype.core_ui.features.wallpaper

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemWallpaperSelectGradientBinding
import com.anytypeio.anytype.core_ui.databinding.ItemWallpaperSelectSectionBinding
import com.anytypeio.anytype.core_ui.extensions.tint
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.wallpaper.WallpaperColor
import com.anytypeio.anytype.presentation.wallpaper.WallpaperSelectView
import com.anytypeio.anytype.presentation.wallpaper.WallpaperView

class WallpaperSelectAdapter(
    val onWallpaperSelected: (WallpaperView) -> Unit
) : RecyclerView.Adapter<WallpaperSelectAdapter.VH>() {

    private var items: List<WallpaperSelectView> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = when (viewType) {
        R.layout.item_wallpaper_select_section -> SectionViewHolder(
            binding = ItemWallpaperSelectSectionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        R.layout.item_wallpaper_select_gradient -> GradientViewHolder(
            ItemWallpaperSelectGradientBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ).apply {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val item = items[pos]
                    if (item is WallpaperSelectView.Wallpaper)
                        onWallpaperSelected(item.item)
                }
            }
        }
        R.layout.item_wallpaper_select_solid_color -> SolidColorViewHolder(parent).apply {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val item = items[pos]
                    if (item is WallpaperSelectView.Wallpaper)
                        onWallpaperSelected(item.item)
                }
            }
        }
        else -> throw IllegalStateException("Unexpected view type: $viewType")
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        when (holder) {
            is GradientViewHolder -> {
                val item = items[position]
                check(item is WallpaperSelectView.Wallpaper)
                holder.bind(item)
            }
            is SolidColorViewHolder -> {
                val item = items[position]
                check(item is WallpaperSelectView.Wallpaper)
                holder.bind(item)
            }
            is SectionViewHolder -> {
                val item = items[position]
                check(item is WallpaperSelectView.Section)
                holder.bind(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (val view = items[position]) {
        is WallpaperSelectView.Section -> R.layout.item_wallpaper_select_section
        is WallpaperSelectView.Wallpaper -> {
            if (view.item is WallpaperView.SolidColor)
                R.layout.item_wallpaper_select_solid_color
            else
                R.layout.item_wallpaper_select_gradient
        }
    }

    fun update(views: List<WallpaperSelectView>) {
        items = views
        notifyDataSetChanged()
    }

    sealed class VH(view: View) : RecyclerView.ViewHolder(view)

    class SectionViewHolder(val binding: ItemWallpaperSelectSectionBinding) : VH(binding.root) {
        fun bind(item: WallpaperSelectView.Section) = with(binding) {
            when (item) {
                WallpaperSelectView.Section.Gradient -> {
                    tvSectionName.setText(R.string.cover_gradients)
                }
                WallpaperSelectView.Section.SolidColor -> {
                    tvSectionName.setText(R.string.cover_color_solid)
                }
                else -> {}
            }
        }
    }

    class SolidColorViewHolder(parent: ViewGroup) : VH(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_wallpaper_select_solid_color,
            parent,
            false
        )
    ) {
        fun bind(wallpaper: WallpaperSelectView.Wallpaper) = with(itemView) {
            val item = wallpaper.item
            check(item is WallpaperView.SolidColor)
            val color = WallpaperColor.values().find { it.code == item.code }
            if (color != null) {
                itemView.tint(Color.parseColor(color.hex))
            } else {
                itemView.tint(Color.WHITE)
            }
            itemView.background.alpha = WallpaperView.WALLPAPER_DEFAULT_ALPHA
        }
    }

    class GradientViewHolder(
        private val binding: ItemWallpaperSelectGradientBinding
    ) : VH(binding.root) {
        fun bind(item: WallpaperSelectView.Wallpaper) = with(binding.gradient) {
            val wallpaper = item.item
            check(wallpaper is WallpaperView.Gradient)
            when (wallpaper.code) {
                CoverGradient.YELLOW -> setBackgroundResource(R.drawable.cover_gradient_yellow)
                CoverGradient.RED -> setBackgroundResource(R.drawable.cover_gradient_red)
                CoverGradient.BLUE -> setBackgroundResource(R.drawable.cover_gradient_blue)
                CoverGradient.TEAL -> setBackgroundResource(R.drawable.cover_gradient_teal)
                CoverGradient.PINK_ORANGE -> setBackgroundResource(R.drawable.wallpaper_gradient_1)
                CoverGradient.BLUE_PINK -> setBackgroundResource(R.drawable.wallpaper_gradient_2)
                CoverGradient.GREEN_ORANGE -> setBackgroundResource(R.drawable.wallpaper_gradient_3)
                CoverGradient.SKY -> setBackgroundResource(R.drawable.wallpaper_gradient_4)
            }
            binding.gradient.background.alpha = WallpaperView.WALLPAPER_DEFAULT_ALPHA
        }
    }
}