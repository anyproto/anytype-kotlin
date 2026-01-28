package com.anytypeio.anytype.core_ui.extensions

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.webkit.MimeTypeMap
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.RelativeDate
import com.anytypeio.anytype.core_models.ui.CustomIconColor
import com.anytypeio.anytype.core_models.ui.MimeCategory
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.const.MimeTypes
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView

fun Context.drawable(
    @DrawableRes id: Int
): Drawable =
    ContextCompat.getDrawable(this, id) ?: throw IllegalArgumentException("Resource not found")

fun Context.color(
    @ColorRes id: Int
) = ContextCompat.getColor(this, id)

fun SimpleRelationView.icon(): Int {
    val formatIcon = format.simpleIcon()
    return if (this.key == Relations.NAME) {
        R.drawable.ic_relation_name
    } else {
        formatIcon
    }
}

fun RelationFormat.simpleIcon(): Int = when (this) {
    RelationFormat.SHORT_TEXT -> R.drawable.ic_relation_format_text_small
    RelationFormat.LONG_TEXT -> R.drawable.ic_relation_format_text_small
    RelationFormat.NUMBER -> R.drawable.ic_relation_format_number_small
    RelationFormat.STATUS -> R.drawable.ic_relation_format_status_small
    RelationFormat.TAG -> R.drawable.ic_relation_format_tag_small
    RelationFormat.DATE -> R.drawable.ic_relation_format_date_small
    RelationFormat.FILE -> R.drawable.ic_relation_format_attachment_small
    RelationFormat.CHECKBOX -> R.drawable.ic_relation_format_checkbox_small
    RelationFormat.URL -> R.drawable.ic_relation_format_url_small
    RelationFormat.EMAIL -> R.drawable.ic_relation_format_email_small
    RelationFormat.PHONE -> R.drawable.ic_relation_format_phone_number_small
    RelationFormat.OBJECT -> R.drawable.ic_relation_format_object_small
    else -> R.drawable.ic_relation_format_text_small
}

fun DVSortType.text(format: RelationFormat): Int = when (format) {
    RelationFormat.TAG, RelationFormat.STATUS -> {
        if (this == DVSortType.ASC)
            R.string.sort_ascending
        else
            R.string.sort_descending
    }
    RelationFormat.NUMBER, RelationFormat.DATE -> {
        if (this == DVSortType.ASC)
            R.string.sort_from_one_to_nine
        else
            R.string.sort_from_nine_to_one
    }
    RelationFormat.CHECKBOX -> {
        if (this == DVSortType.ASC)
            R.string.sort_from_unchecked_to_checked
        else
            R.string.sort_from_checked_to_unchecked
    }
    else -> {
        if (this == DVSortType.ASC)
            R.string.sort_from_a_to_z
        else
            R.string.sort_from_z_to_a
    }
}

fun String?.getMimeIcon(extension: String?): Int {
    // First check extension for known types that may not be properly handled by Android
    var mime = when (extension?.lowercase()) {
        "bmp" -> "image/bmp"
        "tiff", "tif" -> "image/tiff"
        "webp" -> "image/webp"
        "svg" -> "image/svg+xml"
        "avif" -> "image/avif"
        "apng" -> "image/apng"
        "flac" -> "audio/flac"
        "m4a" -> "audio/m4a"
        else -> null
    }
    
    // If no explicit override, try Android's MimeTypeMap
    if (mime == null) {
        mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
    
    // If still null, fall back to the provided MIME type
    if (mime.isNullOrBlank()) {
        mime = this
    }
    
    return when (MimeTypes.category(mime)) {
        MimeTypes.Category.PDF -> R.drawable.ic_mime_pdf
        MimeTypes.Category.IMAGE -> R.drawable.ic_mime_image
        MimeTypes.Category.AUDIO -> R.drawable.ic_mime_music
        MimeTypes.Category.TEXT -> R.drawable.ic_mime_text
        MimeTypes.Category.VIDEO -> R.drawable.ic_mime_video
        MimeTypes.Category.ARCHIVE -> R.drawable.ic_mime_archive
        MimeTypes.Category.TABLE -> R.drawable.ic_mime_table
        MimeTypes.Category.PRESENTATION -> R.drawable.ic_mime_presentation
        MimeTypes.Category.OTHER -> R.drawable.ic_mime_other
    }
}

fun MimeCategory.getMimeIcon(): Int {
    return when (this) {
        MimeCategory.PDF -> R.drawable.ic_mime_pdf
        MimeCategory.IMAGE -> R.drawable.ic_mime_image
        MimeCategory.AUDIO -> R.drawable.ic_mime_music
        MimeCategory.TEXT -> R.drawable.ic_mime_text
        MimeCategory.VIDEO -> R.drawable.ic_mime_video
        MimeCategory.ARCHIVE -> R.drawable.ic_mime_archive
        MimeCategory.TABLE -> R.drawable.ic_mime_table
        MimeCategory.PRESENTATION -> R.drawable.ic_mime_presentation
        MimeCategory.OTHER -> R.drawable.ic_mime_other
    }
}

@StringRes
fun RelationFormat.getPrettyName(): Int = when (this) {
    RelationFormat.LONG_TEXT, RelationFormat.SHORT_TEXT -> R.string.relation_format_long_text
    RelationFormat.NUMBER -> R.string.relation_format_number
    RelationFormat.STATUS -> R.string.relation_format_status
    RelationFormat.TAG -> R.string.relation_format_tag
    RelationFormat.DATE -> R.string.relation_format_date
    RelationFormat.FILE -> R.string.relation_format_file
    RelationFormat.CHECKBOX -> R.string.relation_format_checkbox
    RelationFormat.URL -> R.string.relation_format_url
    RelationFormat.EMAIL -> R.string.relation_format_email
    RelationFormat.PHONE -> R.string.relation_format_phone
    RelationFormat.EMOJI -> R.string.relation_format_emoji
    RelationFormat.OBJECT -> R.string.relation_format_object
    RelationFormat.RELATIONS -> R.string.relation_format_relation
    RelationFormat.UNDEFINED -> R.string.undefined
}

fun RelativeDate.getPrettyName(
    isTimeIncluded: Boolean = false,
    resources: Resources
): String = when (this) {
    is RelativeDate.Other -> {
        if (isTimeIncluded) {
            "$formattedDate $formattedTime"
        } else {
            formattedDate
        }
    }
    is RelativeDate.Today -> resources.getString(R.string.today)
    is RelativeDate.Tomorrow -> resources.getString(R.string.tomorrow)
    is RelativeDate.Yesterday -> resources.getString(R.string.yesterday)
    RelativeDate.Empty -> ""
}

@ColorRes
fun CustomIconColor.colorRes() = when (this) {
    CustomIconColor.Gray -> R.color.glyph_active
    CustomIconColor.Yellow -> R.color.palette_system_yellow
    CustomIconColor.Amber -> R.color.palette_system_amber_100
    CustomIconColor.Red -> R.color.palette_system_red
    CustomIconColor.Pink -> R.color.palette_system_pink
    CustomIconColor.Purple -> R.color.palette_system_purple
    CustomIconColor.Blue -> R.color.palette_system_blue
    CustomIconColor.Sky -> R.color.palette_system_sky
    CustomIconColor.Teal -> R.color.palette_system_teal
    CustomIconColor.Green -> R.color.palette_system_green
    CustomIconColor.Transparent -> R.color.shape_transparent_primary
}

/**
 * Gets the drawable resource ID for a gradient code
 */
@DrawableRes
fun getGradientDrawableResource(gradientCode: String): Int {
    return when (gradientCode) {
        CoverGradient.YELLOW -> R.drawable.cover_gradient_yellow
        CoverGradient.RED -> R.drawable.cover_gradient_red
        CoverGradient.BLUE -> R.drawable.cover_gradient_blue
        CoverGradient.TEAL -> R.drawable.cover_gradient_teal
        CoverGradient.PINK_ORANGE -> R.drawable.wallpaper_gradient_1
        CoverGradient.BLUE_PINK -> R.drawable.wallpaper_gradient_2
        CoverGradient.GREEN_ORANGE -> R.drawable.wallpaper_gradient_3
        CoverGradient.SKY -> R.drawable.wallpaper_gradient_4
        else -> R.drawable.cover_gradient_default
    }
}