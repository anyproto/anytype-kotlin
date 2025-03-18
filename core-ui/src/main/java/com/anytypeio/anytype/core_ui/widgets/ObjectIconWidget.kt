package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.compose.ui.res.colorResource
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.WidgetObjectIconBinding
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.core_ui.extensions.getMimeIcon
import com.anytypeio.anytype.core_ui.extensions.setCircularShape
import com.anytypeio.anytype.core_ui.extensions.setCorneredShape
import com.anytypeio.anytype.core_ui.extensions.tint
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor.Companion.fromIconOption
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import timber.log.Timber

class ObjectIconWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        const val DEFAULT_SIZE = 28
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
            is ObjectIcon.Profile.Avatar -> setProfileInitials(icon.name)
            is ObjectIcon.Profile.Image -> setCircularImage(icon.hash)
            is ObjectIcon.Task -> setTask(icon.isChecked)
            is ObjectIcon.Bookmark -> setBookmark(icon.image)
            is ObjectIcon.None -> removeIcon()
            is ObjectIcon.File -> setFileImage(
                mime = icon.mime,
                extension = icon.extensions
            )
            ObjectIcon.Deleted -> setDeletedIcon()
            is ObjectIcon.Checkbox -> setCheckbox(icon.isChecked)
            is ObjectIcon.Empty -> icon.setEmptyIcon()
            is ObjectIcon.ObjectType -> setCustomIcon(icon)
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

    private fun setProfileInitials(
        name: String
    ) {
        with(binding) {
            ivImage.invisible()
            emojiContainer.invisible()
            ivCheckbox.invisible()
            ivBookmark.setImageDrawable(null)
            ivBookmark.gone()
            initialContainer.visible()
            initialContainer.setBackgroundResource(R.drawable.object_in_list_background_profile_initial)
            initial.setTextColor(context.color(R.color.text_white))
            initial.setHintTextColor(context.color(R.color.text_white))
            initial.text = name.firstOrNull()?.uppercaseChar()?.toString()
        }
    }

    private fun setEmoji(emoji: String?) {
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
                val adapted = Emojifier.safeUri(emoji)
                if (adapted != Emojifier.Config.EMPTY_URI) {
                    binding.tvEmojiFallback.gone()
                    Glide
                        .with(this)
                        .load(Emojifier.uri(emoji))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.ivEmoji)
                } else {
                    binding.ivEmoji.setImageDrawable(null)
                    binding.tvEmojiFallback.text = emoji
                    binding.tvEmojiFallback.visible()
                }
            } catch (e: Throwable) {
                Timber.w(e, "Error while setting emoji icon for: $emoji")
            }
        } else {
            binding.ivEmoji.setImageDrawable(null)
        }
    }

    private fun setFileImage(mime: String?, extension: String?) {
        val icon = mime.getMimeIcon(extension)
        with(binding) {
            ivImage.visible()
            ivImage.scaleType = ImageView.ScaleType.CENTER_CROP
            ivImage.setImageResource(icon)
            ivCheckbox.invisible()
            initialContainer.invisible()
            ivBookmark.invisible()
            emojiContainer.invisible()
        }
    }

    private fun setCircularImage(image: Url?) {
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

    private fun setRectangularImage(image: Url?) {
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

    private fun setTask(isChecked: Boolean?) {
        with(binding) {
            ivCheckbox.visible()
            ivCheckbox.background = context.drawable(R.drawable.ic_data_view_grid_checkbox_selector)
            ivCheckbox.isActivated = isChecked ?: false
            initialContainer.invisible()
            emojiContainer.invisible()
            ivBookmark.gone()
            ivBookmark.setImageDrawable(null)
            ivImage.invisible()
        }
    }

    private fun setCheckbox(isChecked: Boolean?) {
        with(binding) {
            ivCheckbox.background = context.drawable(R.drawable.ic_relation_checkbox_selector)
            ivCheckbox.scaleType = ImageView.ScaleType.CENTER_CROP
            ivCheckbox.visible()
            ivCheckbox.isSelected = isChecked ?: false
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

    private fun setDeletedIcon() {
        val icon = context.drawable(R.drawable.ic_relation_deleted)
        with(binding) {
            ivImage.visible()
            ivImage.scaleType = ImageView.ScaleType.CENTER_CROP
            ivImage.setImageDrawable(icon)
            ivCheckbox.invisible()
            initialContainer.invisible()
            ivBookmark.invisible()
            emojiContainer.invisible()
        }
    }

    private fun setCustomIcon(icon: ObjectIcon.ObjectType) {
        val resId = context.resources.getIdentifier(icon.customIconData.icon.drawableName, "drawable", context.packageName)
        with(binding) {
            ivCheckbox.invisible()
            initialContainer.invisible()
            ivImage.invisible()
            ivBookmark.setImageDrawable(null)
            ivBookmark.gone()
            emojiContainer.visible()
        }
        try {
            if (resId != 0) {
                val tint = fromIconOption(iconOption = icon.customIconData.color?.iconOption).color(context)
                binding.tvEmojiFallback.gone()
                binding.ivEmoji.setImageResource(resId)
                binding.ivEmoji.imageTintList = ColorStateList.valueOf(tint)
            } else {
                binding.ivEmoji.setImageDrawable(null)
                //binding.tvEmojiFallback.text = emoji
                binding.tvEmojiFallback.visible()
            }
        } catch (e: Throwable) {
            Timber.w(e, "Error while setting object type icon for")
        }
    }

    private fun ObjectIcon.Empty.setEmptyIcon() {
        val (drawable, containerBackground) = when (this) {
            ObjectIcon.Empty.Bookmark -> R.drawable.ic_empty_state_link to true
            ObjectIcon.Empty.Chat -> R.drawable.ic_empty_state_chat to true
            ObjectIcon.Empty.List -> R.drawable.ic_empty_state_list to true
            ObjectIcon.Empty.ObjectType -> R.drawable.ic_empty_state_type to true
            ObjectIcon.Empty.Page -> R.drawable.ic_empty_state_page to true
            ObjectIcon.Empty.Date -> R.drawable.ic_obj_date_24 to false
        }
        val icon = context.drawable(drawable)
        with(binding) {
            ivEmoji.setImageDrawable(icon)
            ivCheckbox.invisible()
            initialContainer.invisible()
            ivImage.invisible()
            ivBookmark.setImageDrawable(null)
            ivBookmark.gone()
            if (containerBackground) {
                emojiContainer.visible()
            } else {
                emojiContainer.visible()
                emojiContainer.setBackgroundResource(0)
            }
        }
    }

    fun setIvEmojiSize(emojiSize: Int) {
        binding.ivEmoji.updateLayoutParams<LayoutParams> {
            this.height = emojiSize
            this.width = emojiSize
        }
    }
}
