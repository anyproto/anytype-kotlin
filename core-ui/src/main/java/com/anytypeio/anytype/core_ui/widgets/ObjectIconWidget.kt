package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.avatarColor
import com.anytypeio.anytype.core_utils.ext.firstDigitByHash
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.emojifier.Emojifier
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.widget_object_icon.view.*
import timber.log.Timber

class ObjectIconWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        val DEFAULT_SIZE = 24
    }

    init {
        inflate(context)
        setupAttributeValues(attrs)
    }

    internal fun inflate(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.widget_object_icon, this)
    }

    private fun setupAttributeValues(set: AttributeSet?) {
        if (set == null) return

        val attrs = context.obtainStyledAttributes(set, R.styleable.ObjectIconWidget, 0, 0)

        with(attrs) {
            val emojiSize =
                getDimensionPixelSize(R.styleable.ObjectIconWidget_emojiSize, DEFAULT_SIZE)
            val imageSize =
                getDimensionPixelSize(R.styleable.ObjectIconWidget_imageSize, DEFAULT_SIZE)
            val checkboxSize =
                getDimensionPixelSize(R.styleable.ObjectIconWidget_checkboxSize, DEFAULT_SIZE)

            val hasEmojiCircleBackground =
                attrs.getBoolean(R.styleable.ObjectIconWidget_hasEmojiCircleBackground, false)
            val hasEmojiRoundedBackground =
                attrs.getBoolean(R.styleable.ObjectIconWidget_hasEmojiRoundedBackground, false)

            ivEmoji.updateLayoutParams<LayoutParams> {
                this.height = emojiSize
                this.width = emojiSize
            }

            ivImage.updateLayoutParams<LayoutParams> {
                this.height = imageSize
                this.width = imageSize
            }

            ivCheckbox.updateLayoutParams<LayoutParams> {
                this.height = checkboxSize
                this.width = checkboxSize
            }

            if (hasEmojiCircleBackground) {
                emojiContainer.setBackgroundResource(R.drawable.circle_object_icon_emoji_background)
            }

            if (hasEmojiRoundedBackground) {
                emojiContainer.setBackgroundResource(R.drawable.rectangle_object_icon_emoji_background)
            }

            if (!hasEmojiCircleBackground && !hasEmojiRoundedBackground) {
                emojiContainer.background = null
            }

            val initialTextSize =
                attrs.getDimensionPixelSize(R.styleable.ObjectIconWidget_initialTextSize, 0)
            if (initialTextSize > 0) initial.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                initialTextSize.toFloat()
            )
            recycle()
        }
    }

    fun setIcon(
        emoji: String?,
        image: Url?,
        name: String
    ) {
        if (emoji.isNullOrBlank() && image.isNullOrBlank()) {
            setInitials(name)
        } else {
            setEmoji(emoji)
            setImage(image)
        }
        //todo Add checkbox logic
    }

    private fun setInitials(
        name: String
    ) {
        val pos = name.firstDigitByHash()
        val color = context.avatarColor(pos)
        ivImage.invisible()
        emojiContainer.invisible()
        ivCheckbox.invisible()
        initialContainer.visible()
        initialContainer.backgroundTintList = ColorStateList.valueOf(color)
        initial.text = if (name.isNotEmpty()) name.first().toUpperCase().toString() else name
    }

    private fun setEmoji(emoji: String?) {
        if (!emoji.isNullOrBlank()) {
            ivCheckbox.invisible()
            initialContainer.invisible()
            ivImage.invisible()
            emojiContainer.visible()
            try {
                Glide
                    .with(this)
                    .load(Emojifier.uri(emoji))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivEmoji)
            } catch (e: Throwable) {
                Timber.e(e, "Error while setting emoji icon for: ${emoji}")
            }
        } else {
            ivEmoji.setImageDrawable(null)
        }
    }

    private fun setImage(image: Url?) {
        if (!image.isNullOrBlank()) {
            ivCheckbox.invisible()
            initialContainer.invisible()
            emojiContainer.invisible()
            ivImage.visible()
            Glide
                .with(this)
                .load(image)
                .centerInside()
                .circleCrop()
                .into(ivImage)
        } else {
            ivImage.setImageDrawable(null)
        }
    }
}