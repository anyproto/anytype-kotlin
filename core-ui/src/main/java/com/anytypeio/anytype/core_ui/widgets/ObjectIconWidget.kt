package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.`object`.ObjectIcon
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
            val hasEmojiRounded12Background =
                attrs.getBoolean(R.styleable.ObjectIconWidget_hasEmojiRounded12Background, false)
            val hasEmojiRounded8Background =
                attrs.getBoolean(R.styleable.ObjectIconWidget_hasEmojiRounded8Background, false)
            val hasInitialRounded8Background =
                attrs.getBoolean(R.styleable.ObjectIconWidget_hasInitialRounded8Background, false)

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

            if (hasEmojiRounded12Background) {
                emojiContainer.setBackgroundResource(R.drawable.rectangle_object_in_list_emoji_icon)
            }

            if (hasEmojiRounded8Background) {
                emojiContainer.setBackgroundResource(R.drawable.rectangle_object_icon_emoji_background_8)
            }

            if (!hasEmojiCircleBackground && !hasEmojiRounded12Background && !hasEmojiRounded8Background) {
                emojiContainer.background = null
            }

            if (hasInitialRounded8Background) {
                initialContainer.setBackgroundResource(R.drawable.rectangle_avatar_initial_background_8)
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

    fun setIcon(icon: ObjectIcon) {
        when(icon) {
            is ObjectIcon.Basic.Emoji -> setEmoji(icon.unicode)
            is ObjectIcon.Basic.Image -> setRectangularImage(icon.hash)
            is ObjectIcon.Basic.Avatar -> setBasicInitials(icon.name)
            is ObjectIcon.Profile.Avatar -> setProfileInitials(icon.name)
            is ObjectIcon.Profile.Image -> setCircularImage(icon.hash)
            is ObjectIcon.Task -> setCheckbox(icon.isChecked)
        }
    }

    fun setIcon(
        emoji: String?,
        image: Url?,
        name: String
    ) {
        if (emoji.isNullOrBlank() && image.isNullOrBlank()) {
            setProfileInitials(name)
        } else {
            setEmoji(emoji)
            setCircularImage(image)
        }
        //todo Add checkbox logic
    }

    fun setProfileInitials(
        name: String
    ) {
        val textColor = context.color(R.color.default_object_profile_avatar_text_color)
        ivImage.invisible()
        emojiContainer.invisible()
        ivCheckbox.invisible()
        initialContainer.visible()
        rectangularIconContainer.invisible()
        initialContainer.setBackgroundResource(R.drawable.object_in_list_background_profile_initial)
        initial.setTextColor(textColor)
        initial.setHintTextColor(textColor)
        initial.text = name.firstOrNull()?.uppercaseChar()?.toString()
    }

    fun setBasicInitials(
        name: String
    ) {
        val textColor = context.color(R.color.default_object_basic_avatar_text_color)
        ivImage.invisible()
        emojiContainer.invisible()
        ivCheckbox.invisible()
        initialContainer.visible()
        rectangularIconContainer.invisible()
        initialContainer.setBackgroundResource(R.drawable.object_in_list_background_basic_initial)
        initial.setTextColor(textColor)
        initial.setHintTextColor(textColor)
        initial.text = name.firstOrNull()?.uppercaseChar()?.toString()
    }

    fun setEmoji(emoji: String?) {
        if (!emoji.isNullOrBlank()) {
            ivCheckbox.invisible()
            initialContainer.invisible()
            rectangularIconContainer.invisible()
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

    fun setCircularImage(image: Url?) {
        if (!image.isNullOrBlank()) {
            ivCheckbox.invisible()
            initialContainer.invisible()
            emojiContainer.invisible()
            rectangularIconContainer.invisible()
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

    fun setRectangularImage(image: Url?) {
        if (!image.isNullOrBlank()) {
            ivCheckbox.invisible()
            initialContainer.invisible()
            emojiContainer.invisible()
            ivImage.invisible()
            rectangularIconContainer.visible()
            Glide
                .with(this)
                .load(image)
                .centerCrop()
                .into(ivImageRectangular)
        } else {
            rectangularIconContainer.gone()
            ivImage.setImageDrawable(null)
        }
    }

    fun setCheckbox(isChecked: Boolean?) {
        ivCheckbox.visible()
        ivCheckbox.isSelected = isChecked ?: false
        initialContainer.invisible()
        emojiContainer.invisible()
        rectangularIconContainer.invisible()
        ivImage.invisible()
    }
}