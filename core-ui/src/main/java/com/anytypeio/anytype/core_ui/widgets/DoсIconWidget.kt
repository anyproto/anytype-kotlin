package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.ViewOutlineProvider
import androidx.appcompat.widget.AppCompatImageView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.emojifier.Emojifier
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import timber.log.Timber
import kotlin.math.min

class DocIconWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        outlineProvider = ViewOutlineProvider.BACKGROUND
        clipToOutline = true
        setBackgroundResource(R.drawable.rectangle_default_page_logo_background)
    }

    /**
     * Make sure width and height of image are same
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val minDimension = min(width, height)
        setMeasuredDimension(minDimension, minDimension)
    }

    fun setEmoji(unicode: String?) {
        try {
            if (unicode != null) {
                Glide
                    .with(this)
                    .load(Emojifier.uri(unicode))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerInside()
                    .into(this)
            } else {
                setImageDrawable(null)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while setting emoji with unicode: $unicode")
        }
    }
}