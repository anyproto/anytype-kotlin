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
import com.anytypeio.anytype.core_models.RelativeDate
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.const.MimeTypes
import com.anytypeio.anytype.presentation.objects.ObjectLayoutView
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor
import com.anytypeio.anytype.presentation.sets.model.ColumnView

fun Context.drawable(
    @DrawableRes id: Int
): Drawable =
    ContextCompat.getDrawable(this, id) ?: throw IllegalArgumentException("Resource not found")

fun Context.color(
    @ColorRes id: Int
) = ContextCompat.getColor(this, id)

fun Context.avatarColor(
    position: Int
): Int {
    val colors = resources.obtainTypedArray(R.array.avatar_colors)
    val color = colors.getColor(position, 0)
    colors.recycle()
    return color
}

fun ColumnView.Format.relationIcon(isMedium: Boolean = false): Int = when (this) {
    ColumnView.Format.SHORT_TEXT, ColumnView.Format.LONG_TEXT -> {
        if (isMedium)
            R.drawable.ic_relation_text_48
        else
            R.drawable.ic_relation_text_32
    }
    ColumnView.Format.NUMBER -> {
        if (isMedium)
            R.drawable.ic_relation_number_48
        else
            R.drawable.ic_relation_number_32
    }
    ColumnView.Format.STATUS -> {
        if (isMedium)
            R.drawable.ic_relation_status_48
        else
            R.drawable.ic_relation_status_32
    }
    ColumnView.Format.DATE -> {
        if (isMedium)
            R.drawable.ic_relation_date_48
        else
            R.drawable.ic_relation_date_32
    }
    ColumnView.Format.FILE -> {
        if (isMedium)
            R.drawable.ic_relation_attachment_48
        else
            R.drawable.ic_relation_attachment_32
    }
    ColumnView.Format.CHECKBOX -> {
        if (isMedium)
            R.drawable.ic_relation_checkbox_48
        else
            R.drawable.ic_relation_checkbox_32
    }
    ColumnView.Format.URL -> {
        if (isMedium)
            R.drawable.ic_relation_url_48
        else
            R.drawable.ic_relation_url_32
    }
    ColumnView.Format.EMAIL -> {
        if (isMedium)
            R.drawable.ic_relation_email_48
        else
            R.drawable.ic_relation_email_32
    }
    ColumnView.Format.PHONE -> {
        if (isMedium)
            R.drawable.ic_relation_phone_number_48
        else
            R.drawable.ic_relation_phone_number_32
    }
    ColumnView.Format.EMOJI -> R.drawable.ic_relation_object_32
    ColumnView.Format.OBJECT -> {
        if (isMedium)
            R.drawable.ic_relation_object_48
        else
            R.drawable.ic_relation_object_32
    }
    ColumnView.Format.TAG -> {
        if (isMedium)
            R.drawable.ic_relation_tag_48
        else
            R.drawable.ic_relation_tag_32
    }
    else -> {
        // TODO
        R.drawable.circle_solid_default
    }
}

fun ColumnView.Format.relationIconSmall(): Int? = when (this) {
    ColumnView.Format.SHORT_TEXT -> R.drawable.ic_relation_format_text_small
    ColumnView.Format.LONG_TEXT -> R.drawable.ic_relation_format_text_small
    ColumnView.Format.NUMBER -> R.drawable.ic_relation_format_number_small
    ColumnView.Format.STATUS -> R.drawable.ic_relation_format_status_small
    ColumnView.Format.DATE -> R.drawable.ic_relation_format_date_small
    ColumnView.Format.FILE -> R.drawable.ic_relation_format_attachment_small
    ColumnView.Format.CHECKBOX -> R.drawable.ic_relation_format_checkbox_small
    ColumnView.Format.URL -> R.drawable.ic_relation_format_url_small
    ColumnView.Format.EMAIL -> R.drawable.ic_relation_format_email_small
    ColumnView.Format.PHONE -> R.drawable.ic_relation_format_phone_number_small
    ColumnView.Format.OBJECT -> R.drawable.ic_relation_format_object_small
    ColumnView.Format.TAG -> R.drawable.ic_relation_format_tag_small
    else -> null
}

fun RelationFormat.icon(isMedium: Boolean = false): Int = when (this) {
    RelationFormat.SHORT_TEXT, RelationFormat.LONG_TEXT -> {
        if (isMedium)
            R.drawable.ic_relation_text_48
        else
            R.drawable.ic_relation_text_32
    }
    RelationFormat.NUMBER -> {
        if (isMedium)
            R.drawable.ic_relation_number_48
        else
            R.drawable.ic_relation_number_32
    }
    RelationFormat.STATUS -> {
        if (isMedium)
            R.drawable.ic_relation_status_48
        else
            R.drawable.ic_relation_status_32
    }
    RelationFormat.DATE -> {
        if (isMedium)
            R.drawable.ic_relation_date_48
        else
            R.drawable.ic_relation_date_32
    }
    RelationFormat.FILE -> {
        if (isMedium)
            R.drawable.ic_relation_attachment_48
        else
            R.drawable.ic_relation_attachment_32
    }
    RelationFormat.CHECKBOX -> {
        if (isMedium)
            R.drawable.ic_relation_checkbox_48
        else
            R.drawable.ic_relation_checkbox_32
    }
    RelationFormat.URL -> {
        if (isMedium)
            R.drawable.ic_relation_url_48
        else
            R.drawable.ic_relation_url_32
    }
    RelationFormat.EMAIL -> {
        if (isMedium)
            R.drawable.ic_relation_email_48
        else
            R.drawable.ic_relation_email_32
    }
    RelationFormat.PHONE -> {
        if (isMedium)
            R.drawable.ic_relation_phone_number_48
        else
            R.drawable.ic_relation_phone_number_32
    }
    RelationFormat.EMOJI -> R.drawable.ic_relation_object_32
    RelationFormat.OBJECT -> {
        if (isMedium)
            R.drawable.ic_relation_object_48
        else
            R.drawable.ic_relation_object_32
    }
    RelationFormat.TAG -> {
        if (isMedium)
            R.drawable.ic_relation_tag_48
        else
            R.drawable.ic_relation_tag_32
    }
    else -> {
        // TODO
        R.drawable.circle_solid_default
    }
}

fun RelationFormat.simpleIcon(): Int? = when (this) {
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
    else -> null
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
    var mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
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

fun MimeTypes.Category.getMimeIcon(): Int {
    return when (this) {
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