package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetObjectIconCardBinding
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import coil3.load
import coil3.request.CachePolicy
import coil3.request.transformations
import coil3.size.Scale
import coil3.transform.CircleCropTransformation
import coil3.transform.RoundedCornersTransformation
import timber.log.Timber

class ObjectCardIconWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val emojiSize = resources.getDimension(R.dimen.object_icon_card_emoji_size).toInt()
    private val rectangleImageRadius = resources.getDimension(R.dimen.object_icon_card_rectangle_image_radius).toInt()

    val binding = WidgetObjectIconCardBinding.inflate(
        LayoutInflater.from(context), this
    )

    fun bind(icon: ObjectIcon) = with(binding) {
        when(icon) {
            is ObjectIcon.Basic.Emoji -> {
                clearInitials()
                setBackgroundResource(R.drawable.rect_object_icon_card_emoji_background)
                ivIcon.updateLayoutParams<LayoutParams> {
                    height = emojiSize
                    width = emojiSize
                }
                try {
                    ivIcon.load(Emojifier.uri(icon.unicode)) {
                        diskCachePolicy(CachePolicy.ENABLED)
                        memoryCachePolicy(CachePolicy.ENABLED)
                    }
                } catch (e: Throwable) {
                    Timber.w(e, "Error while setting emoji icon for: ${icon.unicode}")
                }
            }
            is ObjectIcon.Basic.Image -> {
                clearInitials()
                setBackgroundResource(0)
                ivIcon.updateLayoutParams<LayoutParams> {
                    height = LayoutParams.MATCH_PARENT
                    width = LayoutParams.MATCH_PARENT
                }
                ivIcon.load(icon.hash) {
                    transformations(RoundedCornersTransformation(rectangleImageRadius.toFloat()))
                }
            }
            is ObjectIcon.Profile.Avatar -> {
                clearInitials()
                setBackgroundResource(R.drawable.circle_default_avatar_background)
                ivIcon.setImageDrawable(null)
                tvInitial.text = icon.name.ifEmpty { DEFAULT_INITIAL_CHAR }.first().uppercaseChar().toString()
            }
            is ObjectIcon.Profile.Image -> {
                clearInitials()
                setBackgroundResource(0)
                ivIcon.updateLayoutParams<LayoutParams> {
                    height = LayoutParams.MATCH_PARENT
                    width = LayoutParams.MATCH_PARENT
                }
                ivIcon.load(icon.hash) {
                    scale(Scale.FIT)
                    transformations(CircleCropTransformation())
                }
            }
            else -> {
                // TODO
            }
        }
    }

    private fun clearInitials() {
        binding.tvInitial.text = null
    }

    companion object {
        const val DEFAULT_INITIAL_CHAR = "U"
    }
}