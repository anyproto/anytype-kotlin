package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.editor.cover.CoverView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop

class GalleryCoverWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    fun bind(cover: CoverView?, fitImage: Boolean = false) {
        when (cover) {
            is CoverView.Color -> {
                setImageDrawable(null)
                setBackgroundColor(cover.coverColor.color)
            }
            is CoverView.Gradient -> {
                setImageDrawable(null)
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
                setImageDrawable(null)
                if (fitImage) {
                    Glide
                        .with(this)
                        .load(cover.url)
                        .fitCenter()
                        .into(this)
                } else {
                    Glide
                        .with(this)
                        .load(cover.url)
                        .transform(
                            CenterCrop(),
                        )
                        .into(this)
                }
            }
            null -> {
                setBackgroundColor(context.getColor(R.color.shape_transparent))
                setImageDrawable(context.getDrawable(R.drawable.bg_cover_default))
                scaleType = ScaleType.CENTER
            }
        }
    }
}