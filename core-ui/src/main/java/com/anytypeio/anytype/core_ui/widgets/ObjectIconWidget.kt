package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
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
import com.anytypeio.anytype.core_ui.extensions.colorRes
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.core_ui.extensions.getMimeIcon
import com.anytypeio.anytype.core_ui.extensions.setCircularShape
import com.anytypeio.anytype.core_ui.extensions.setCorneredShape
import com.anytypeio.anytype.core_ui.widgets.ObjectIconWidget.Companion.DRAWABLE_DIR
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon.TypeIcon
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor
import coil3.load
import coil3.request.CachePolicy
import com.anytypeio.anytype.presentation.objects.ObjectIcon.TypeIcon.*
import timber.log.Timber

class ObjectIconWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        const val DEFAULT_SIZE = 28
        const val DRAWABLE_DIR = "drawable"
    }

    val binding = WidgetObjectIconBinding.inflate(
        LayoutInflater.from(context), this
    )

    private var imageCornerRadius: Float = 0F
    private var isImageWithCorners: Boolean = false
    private val density = context.resources.displayMetrics.density

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

        ivEmoji.updateLayoutParams<LayoutParams> {
            this.height = emojiSize
            this.width = emojiSize
        }

        tvEmojiFallback.updateLayoutParams<LayoutParams> {
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
        // Reset backgrounds
        binding.emojiContainer.background = null
        binding.ivImage.background = null

        when (icon) {
            is ObjectIcon.Basic.Emoji -> {
                setEmoji(emoji = icon.unicode, fallback = icon.fallback)
            }

            is ObjectIcon.Basic.Image -> {
                setRectangularImage(icon.hash)
            }

            is ObjectIcon.Profile.Avatar -> {
                setProfileInitials(icon.name)
            }

            is ObjectIcon.Profile.Image -> {
                setCircularImage(icon.hash)
            }

            is ObjectIcon.Task -> {
                setTask(icon.isChecked)
            }

            is ObjectIcon.Bookmark -> {
                setBookmark(icon.image)
            }

            is ObjectIcon.None -> {
                removeIcon()
            }

            is ObjectIcon.File -> {
                setFileImage(
                    mime = icon.mime,
                    extension = icon.extensions
                )
            }

            ObjectIcon.Deleted -> {
                setDeletedIcon()
            }

            is ObjectIcon.Checkbox -> {
                setCheckbox(icon.isChecked)
            }

            is TypeIcon.Fallback -> {
                setTypeIcon(icon)
            }

            is TypeIcon.Default -> {
                setTypeIcon(icon)
            }

            TypeIcon.Deleted -> {
                setDeletedIcon()
            }

            is TypeIcon.Emoji -> {
                setEmoji(
                    emoji = icon.unicode,
                    fallback = Fallback(rawValue = icon.rawValue)
                )
            }

            is ObjectIcon.FileDefault -> {
                val iconRes = icon.mime.getMimeIcon()
                setFileIconWithBackground(iconRes)
            }

            is ObjectIcon.SimpleIcon -> {}
        }
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
            initial.setHintTextColor(context.color(R.color.glyph_active))
            initial.text = name.firstOrNull()?.uppercaseChar()?.toString()

            // Set font size according to Compose mapping, based on icon size (width in dp)
            initial.post {
                val widthPx = initialContainer.width
                val sizeDp = widthPx / density
                initial.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    getAvatarFontSizeSp(sizeDp).toFloat()
                )
            }
        }
    }

    private fun setEmoji(
        emoji: String?,
        fallback: ObjectIcon.TypeIcon.Fallback
    ) {
        binding.emojiContainer.background = null
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
                    binding.ivEmoji.visible()
                    binding.ivEmoji.load(adapted) {
                        diskCachePolicy(CachePolicy.ENABLED)
                        memoryCachePolicy(CachePolicy.ENABLED)
                    }
                } else {
                    setTypeIcon(fallback)
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
        setFileIconWithBackground(icon)
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
            }
            binding.ivImage.load(image)
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
                ivImage.visible()
                ivImage.setCorneredShape(imageCornerRadius)
                ivImage.load(image)
            }
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
            binding.ivBookmark.load(image)
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

    private fun setTypeIcon(icon: ObjectIcon.TypeIcon) {

        val (resId, tint) = icon.getDrawableAndTintColor(context)

        with(binding) {
            ivCheckbox.invisible()
            initialContainer.invisible()
            ivImage.invisible()
            ivBookmark.setImageDrawable(null)
            ivBookmark.gone()
            emojiContainer.visible()
            ivEmoji.gone()
            tvEmojiFallback.visible()
        }
        try {
            binding.tvEmojiFallback.setImageResource(resId)
            binding.tvEmojiFallback.imageTintList = ColorStateList.valueOf(tint)
        } catch (e: Throwable) {
            Timber.w(e, "Error while setting object type icon for")
        }
    }

    private fun setFileIconWithBackground(iconRes: Int) {
        with(binding) {
            ivImage.visible()
            ivImage.scaleType = ImageView.ScaleType.FIT_XY
            ivCheckbox.invisible()
            initialContainer.invisible()
            ivBookmark.invisible()
            emojiContainer.invisible()

            ivImage.post {
                val iconDrawable = context.drawable(iconRes)

                val backgroundDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(context.color(R.color.shape_transparent_secondary))
                    cornerRadius = getCornerRadiusInPx()
                }

                val layers = arrayOf(backgroundDrawable, iconDrawable)
                val layerDrawable = LayerDrawable(layers)

                val horizontalMargin = (4 * density).toInt()
                layerDrawable.setLayerInset(0, horizontalMargin, 0, horizontalMargin, 0)

                ivImage.setImageDrawable(layerDrawable)
            }
        }
    }

    private fun getCornerRadiusInPx(): Float {
        val radiusInDp = 2f
        return radiusInDp * density
    }

    private fun getAvatarFontSizeSp(sizeDp: Float): Int {
        return when {
            sizeDp <= 17 -> 10
            sizeDp <= 19 -> 11
            sizeDp <= 21 -> 13
            sizeDp <= 25 -> 14
            sizeDp <= 29 -> 16
            sizeDp <= 31 -> 20
            sizeDp <= 39 -> 20
            sizeDp <= 47 -> 24
            sizeDp <= 63 -> 28
            sizeDp <= 95 -> 40
            sizeDp <= 127 -> 64
            else -> 72
        }
    }
}

fun TypeIcon.getDrawableAndTintColor(context: Context): Pair<Int, Int> {
    val icon = this
    return when (icon) {
        is ObjectIcon.TypeIcon.Default -> {
            val resId = context.resources.getIdentifier(
                icon.drawableResId,
                DRAWABLE_DIR,
                context.packageName
            )
            if (resId != 0) {
                resId to context.getColor(icon.color.colorRes())
            } else {
                0 to 0
            }
        }

        ObjectIcon.TypeIcon.Deleted -> 0 to 0
        is ObjectIcon.TypeIcon.Emoji -> 0 to 0
        is ObjectIcon.TypeIcon.Fallback -> {
            val resId = context.resources.getIdentifier(
                icon.drawableResId,
                DRAWABLE_DIR,
                context.packageName
            )
            if (resId != 0) {
                resId to context.getColor(CustomIconColor.Transparent.colorRes())
            } else {
                val defaultFallback = ObjectIcon.TypeIcon.Fallback.DEFAULT
                context.resources.getIdentifier(
                    defaultFallback.drawableResId,
                    DRAWABLE_DIR,
                    context.packageName
                ) to context.getColor(CustomIconColor.Transparent.colorRes())
            }
        }
    }
}
