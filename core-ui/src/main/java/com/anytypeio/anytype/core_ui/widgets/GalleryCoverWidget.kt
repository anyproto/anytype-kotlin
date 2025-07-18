package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import coil3.load
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.editor.cover.CoverView
import com.anytypeio.anytype.presentation.sets.model.Viewer

class GalleryCoverWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    fun bind(item: Viewer.GalleryView.Item.Cover) {
        when (val cover = item.cover) {
            is CoverView.Color -> {
                visible()
                setImageDrawable(null)
                setBackgroundResource(0)
                setBackgroundColor(cover.coverColor.color)
            }
            is CoverView.Gradient -> {
                visible()
                setImageDrawable(null)
                setBackgroundColor(0)
                when (cover.gradient) {
                    CoverGradient.YELLOW -> setBackgroundResource(R.drawable.cover_gradient_yellow_rounded)
                    CoverGradient.RED -> setBackgroundResource(R.drawable.cover_gradient_red_rounded)
                    CoverGradient.BLUE -> setBackgroundResource(R.drawable.cover_gradient_blue_rounded)
                    CoverGradient.TEAL -> setBackgroundResource(R.drawable.cover_gradient_teal_rounded)
                    CoverGradient.PINK_ORANGE -> setBackgroundResource(R.drawable.wallpaper_gradient_1)
                    CoverGradient.BLUE_PINK -> setBackgroundResource(R.drawable.wallpaper_gradient_2)
                    CoverGradient.GREEN_ORANGE -> setBackgroundResource(R.drawable.wallpaper_gradient_3)
                    CoverGradient.SKY -> setBackgroundResource(R.drawable.wallpaper_gradient_4)
                }
            }
            is CoverView.Image -> {
                visible()
                setImageDrawable(null)
                setBackgroundColor(0)
                setBackgroundResource(0)
                scaleType = if (item.fitImage) ScaleType.FIT_CENTER else ScaleType.CENTER_CROP
                load(cover.url)
            }
            null -> {
                gone()
            }
        }
    }
}