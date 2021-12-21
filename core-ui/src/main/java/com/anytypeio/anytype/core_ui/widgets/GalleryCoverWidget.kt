package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.tint
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.editor.cover.CoverView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners

class GalleryCoverWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private val radius = resources.getDimension(R.dimen.dp_16)

    fun bind(cover: CoverView, fitImage: Boolean = false) {
        when (cover) {
            is CoverView.Color -> {
                setImageDrawable(null)
                val value = CoverColor.values().find { it.code == cover.color }
                if (value != null) {
                    setBackgroundResource(R.drawable.cover_solid_shape_rounded)
                    tint(value.color)
                }
            }
            is CoverView.Gradient -> {
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
                setImageDrawable(null)
                setBackgroundColor(0)
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
                            GranularRoundedCorners(radius, radius, 0f, 0f)
                        )
                        .into(this)
                }
            }
        }
    }
}