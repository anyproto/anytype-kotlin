package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.`object`.ObjectIcon
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.widget_object_icon_card.view.*
import timber.log.Timber

class ObjectCardIconWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val emojiSize = resources.getDimension(R.dimen.object_icon_card_emoji_size).toInt()
    private val rectangleImageRadius = resources.getDimension(R.dimen.object_icon_card_rectangle_image_radius).toInt()

    init {
        View.inflate(context, R.layout.widget_object_icon_card, this)
    }

    fun bind(icon: ObjectIcon) {
        when(icon) {
            is ObjectIcon.Basic.Emoji -> {
                setBackgroundResource(R.drawable.rect_object_icon_card_emoji_background)
                ivIcon.updateLayoutParams<LayoutParams> {
                    height = emojiSize
                    width = emojiSize
                }
                try {
                    Glide
                        .with(this)
                        .load(Emojifier.uri(icon.unicode))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(ivIcon)
                } catch (e: Throwable) {
                    Timber.e(e, "Error while setting emoji icon for: ${icon.unicode}")
                }
            }
            is ObjectIcon.Basic.Image -> {
                setBackgroundResource(0)
                ivIcon.updateLayoutParams<LayoutParams> {
                    height = LayoutParams.MATCH_PARENT
                    width = LayoutParams.MATCH_PARENT
                }
                Glide
                    .with(this)
                    .load(icon.hash)
                    .transform(
                        CenterCrop(),
                        RoundedCorners(rectangleImageRadius)
                    )
                    .into(ivIcon)
            }
            is ObjectIcon.Profile.Avatar -> {
                setBackgroundResource(R.drawable.circle_default_avatar_background)
                ivIcon.setImageDrawable(null)
                tvInitial.text = icon.name.ifEmpty { DEFAULT_INITIAL_CHAR }.first().uppercaseChar().toString()
            }
            is ObjectIcon.Profile.Image -> {
                setBackgroundResource(0)
                ivIcon.updateLayoutParams<LayoutParams> {
                    height = LayoutParams.MATCH_PARENT
                    width = LayoutParams.MATCH_PARENT
                }
                Glide
                    .with(this)
                    .load(icon.hash)
                    .centerInside()
                    .circleCrop()
                    .into(ivIcon)
            }
            else -> {
                // TODO
            }
        }
    }

    companion object {
        const val DEFAULT_INITIAL_CHAR = "U"
    }
}