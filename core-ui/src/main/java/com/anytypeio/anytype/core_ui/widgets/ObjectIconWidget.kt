package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetObjectIconBinding
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.extensions.getMimeIcon
import com.anytypeio.anytype.core_ui.extensions.setCircularShape
import com.anytypeio.anytype.core_ui.extensions.setCorneredShape
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import timber.log.Timber

class ObjectIconWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        const val DEFAULT_SIZE = 24
    }

    val binding = WidgetObjectIconBinding.inflate(
        LayoutInflater.from(context), this
    )

    private var imageCornerRadius: Float = 0F
    private var isImageWithCorners: Boolean = false

    init {
        setupAttributeValues(attrs)
    }

    val checkbox: View get() = binding.ivCheckbox

    private fun setupAttributeValues(set: AttributeSet?) = with(binding) {
        if (set == null) return

        val attrs = context.obtainStyledAttributes(set, R.styleable.ObjectIconWidget, 0, 0)

        val emojiSize =
            attrs.getDimensionPixelSize(R.styleable.ObjectIconWidget_emojiSize, DEFAULT_SIZE)
        val imageSize =
            attrs.getDimensionPixelSize(R.styleable.ObjectIconWidget_imageSize, DEFAULT_SIZE)
        val checkboxSize =
            attrs.getDimensionPixelSize(R.styleable.ObjectIconWidget_checkboxSize, DEFAULT_SIZE)

        val hasEmojiCircleBackground =
            attrs.getBoolean(R.styleable.ObjectIconWidget_hasEmojiCircleBackground, false)
        val hasEmojiRounded12Background =
            attrs.getBoolean(R.styleable.ObjectIconWidget_hasEmojiRounded12Background, false)
        val hasEmojiRounded8Background =
            attrs.getBoolean(R.styleable.ObjectIconWidget_hasEmojiRounded8Background, false)
        val hasInitialRounded8Background =
            attrs.getBoolean(R.styleable.ObjectIconWidget_hasInitialRounded8Background, false)
        val hasEmojiRounded10Background =
            attrs.getBoolean(R.styleable.ObjectIconWidget_hasEmojiRounded10Background, false)
        val hasInitialRoundedCornerBackground =
            attrs.getBoolean(R.styleable.ObjectIconWidget_hasInitialRoundedCornerBackground, false)

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

        if (!hasEmojiCircleBackground && !hasEmojiRounded12Background && !hasEmojiRounded8Background
            && !hasEmojiRounded10Background
        ) {
            emojiContainer.background = null
        }

        if (hasInitialRounded8Background) {
            initialContainer.setBackgroundResource(R.drawable.rectangle_avatar_initial_background_8)
        }

        if (hasEmojiRounded10Background) {
            emojiContainer.setBackgroundResource(R.drawable.bg_rect_10_radius)
        }

        if (hasInitialRoundedCornerBackground) {
            initialContainer.setBackgroundResource(R.drawable.bg_circle_with_corner)
        }

        val initialTextSize =
            attrs.getDimensionPixelSize(R.styleable.ObjectIconWidget_initialTextSize, 0)
        if (initialTextSize > 0) initial.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            initialTextSize.toFloat()
        )

        imageCornerRadius =
            attrs.getDimensionPixelSize(R.styleable.ObjectIconWidget_imageCornerRadius, 2)
                .toFloat()

        isImageWithCorners =
            attrs.getBoolean(R.styleable.ObjectIconWidget_isImageWithCorners, false)
        attrs.recycle()
    }

    fun setIcon(icon: ObjectIcon) {
        when (icon) {
            is ObjectIcon.Basic.Emoji -> setEmoji(icon.unicode)
            is ObjectIcon.Basic.Image -> setRectangularImage(icon.hash)
            is ObjectIcon.Basic.Avatar -> setBasicInitials(icon.name)
            is ObjectIcon.Profile.Avatar -> setProfileInitials(icon.name)
            is ObjectIcon.Profile.Image -> setCircularImage(icon.hash)
            is ObjectIcon.Task -> setCheckbox(icon.isChecked)
            is ObjectIcon.Bookmark -> setBookmark(icon.image)
            is ObjectIcon.None -> removeIcon()
            is ObjectIcon.File -> setFileImage(
                mime = icon.mime,
                fileName = icon.fileName
            )
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

    fun setNonExistentIcon() {
        binding.ivImage.setImageResource(R.drawable.ic_non_existent_object)
    }

    fun setProfileInitials(
        name: String
    ) {
        with(binding) {
            ivImage.invisible()
            emojiContainer.invisible()
            ivCheckbox.invisible()
            ivBookmark.setImageDrawable(null)
            ivBookmark.gone()
            initialContainer.visible()
            if (initialContainer.background == null) {
                initialContainer.setBackgroundResource(R.drawable.object_in_list_background_profile_initial)
            }
            initial.setTextColor(context.color(R.color.text_white))
            initial.setHintTextColor(context.color(R.color.text_tertiary))
            initial.text = name.firstOrNull()?.uppercaseChar()?.toString()
        }
    }

    private fun setBasicInitials(name: String) {
        val textColor = context.color(R.color.text_tertiary)
        with(binding) {
            ivImage.invisible()
            emojiContainer.invisible()
            ivCheckbox.invisible()
            ivBookmark.setImageDrawable(null)
            ivBookmark.gone()
            initialContainer.visible()
            if (initialContainer.background == null) {
                initialContainer.setBackgroundResource(R.drawable.object_in_list_background_basic_initial)
            }
            initial.setTextColor(textColor)
            initial.setHintTextColor(textColor)
            initial.text = name.firstOrNull()?.uppercaseChar()?.toString()
        }
    }

    fun setEmoji(emoji: String?) {
        if (!emoji.isNullOrBlank()) {
            with(binding) {
                ivCheckbox.invisible()
                initialContainer.invisible()
                ivImage.invisible()
                ivBookmark.setImageDrawable(null)
                ivBookmark.gone()
                emojiContainer.visible()
            }
            try {
                Glide
                    .with(this)
                    .load(Emojifier.uri(emoji))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.ivEmoji)
            } catch (e: Throwable) {
                Timber.e(e, "Error while setting emoji icon for: $emoji")
            }
        } else {
            binding.ivEmoji.setImageDrawable(null)
        }
    }

    private fun setFileImage(mime: String?, fileName: String?) {
        val icon = mime.getMimeIcon(fileName)
        with(binding) {
            ivImage.visible()
            ivImage.scaleType = ImageView.ScaleType.CENTER
            ivImage.setImageResource(icon)
            ivCheckbox.invisible()
            initialContainer.invisible()
            ivBookmark.invisible()
            emojiContainer.invisible()
        }
    }

    fun setCircularImage(image: Url?) {
        if (!image.isNullOrBlank()) {
            with(binding) {
                ivCheckbox.invisible()
                initialContainer.invisible()
                emojiContainer.invisible()
                ivImage.visible()
                ivBookmark.setImageDrawable(null)
                ivBookmark.gone()
                ivImage.setCircularShape()
                if (isImageWithCorners) {
                    ivImage.setStrokeWidthResource(R.dimen.dp_2)
                    ivImage.strokeColor =
                        this.root.context.getColorStateList(R.color.background_primary)
                }
            }
            Glide
                .with(this)
                .load(image)
                .centerCrop()
                .into(binding.ivImage)
        } else {
            binding.ivImage.setImageDrawable(null)
        }
    }

    fun setRectangularImage(image: Url?) {
        if (!image.isNullOrBlank()) {
            with(binding) {
                ivCheckbox.invisible()
                initialContainer.invisible()
                emojiContainer.invisible()
                ivBookmark.gone()
                ivBookmark.setImageDrawable(null)
                ivImage.visible()
                ivImage.setCorneredShape(imageCornerRadius)
                if (isImageWithCorners) {
                    ivImage.setStrokeWidthResource(R.dimen.dp_2)
                    ivImage.strokeColor =
                        this.root.context.getColorStateList(R.color.background_primary)
                }
            }
            Glide
                .with(this)
                .load(image)
                .centerCrop()
                .into(binding.ivImage)
        } else {
            binding.ivImage.setImageDrawable(null)
        }
    }

    fun setImageDrawable(drawable: Drawable) {
        with(binding) {
            ivCheckbox.invisible()
            initialContainer.invisible()
            ivImage.invisible()
            emojiContainer.visible()
            ivBookmark.gone()
            ivBookmark.setImageDrawable(null)
            ivEmoji.setImageDrawable(drawable)
        }
    }

    fun setCheckbox(isChecked: Boolean?) {
        with(binding) {
            ivCheckbox.visible()
            ivCheckbox.isActivated = isChecked ?: false
            initialContainer.invisible()
            emojiContainer.invisible()
            ivBookmark.gone()
            ivBookmark.setImageDrawable(null)
            ivImage.invisible()
        }
    }

    private fun setBookmark(image: Url) {
        with(binding) {
            ivCheckbox.invisible()
            initialContainer.invisible()
            emojiContainer.invisible()
            ivImage.invisible()
            ivBookmark.visible()
            Glide
                .with(binding.ivBookmark)
                .load(image)
                .centerCrop()
                .into(binding.ivBookmark)
        }
    }

    private fun removeIcon() {
        with(binding) {
            ivEmoji.setImageDrawable(null)
            ivImage.setImageDrawable(null)
            ivBookmark.setImageDrawable(null)
            ivCheckbox.invisible()
        }
    }
}