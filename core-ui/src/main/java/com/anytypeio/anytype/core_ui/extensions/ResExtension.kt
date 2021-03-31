package com.anytypeio.anytype.core_ui.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.const.MimeTypes
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

fun Context.formatIcon(
    format: ColumnView.Format
): Drawable = drawable(
    when (format) {
        ColumnView.Format.SHORT_TEXT -> R.drawable.ic_text
        ColumnView.Format.LONG_TEXT -> R.drawable.ic_text
        ColumnView.Format.NUMBER -> R.drawable.ic_number
        ColumnView.Format.STATUS -> R.drawable.ic_select
        ColumnView.Format.DATE -> R.drawable.ic_date
        ColumnView.Format.FILE -> R.drawable.ic_file
        ColumnView.Format.CHECKBOX -> R.drawable.ic_checkbox
        ColumnView.Format.URL -> R.drawable.ic_url
        ColumnView.Format.EMAIL -> R.drawable.ic_email
        ColumnView.Format.PHONE -> R.drawable.ic_phone
        ColumnView.Format.EMOJI -> R.drawable.ic_person
        //todo Add proper icon for object
        ColumnView.Format.OBJECT -> R.drawable.ic_multiselect
        ColumnView.Format.TAG -> R.drawable.ic_multiselect
        else -> {
            // TODO
            R.drawable.circle_solid_default
        }
    }
)

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

fun Relation.Format.icon(isMedium: Boolean = false): Int = when (this) {
    Relation.Format.SHORT_TEXT, Relation.Format.LONG_TEXT -> {
        if (isMedium)
            R.drawable.ic_relation_text_48
        else
            R.drawable.ic_relation_text_32
    }
    Relation.Format.NUMBER -> {
        if (isMedium)
            R.drawable.ic_relation_number_48
        else
            R.drawable.ic_relation_number_32
    }
    Relation.Format.STATUS -> {
        if (isMedium)
            R.drawable.ic_relation_status_48
        else
            R.drawable.ic_relation_status_32
    }
    Relation.Format.DATE -> {
        if (isMedium)
            R.drawable.ic_relation_date_48
        else
            R.drawable.ic_relation_date_32
    }
    Relation.Format.FILE -> {
        if (isMedium)
            R.drawable.ic_relation_attachment_48
        else
            R.drawable.ic_relation_attachment_32
    }
    Relation.Format.CHECKBOX -> {
        if (isMedium)
            R.drawable.ic_relation_checkbox_48
        else
            R.drawable.ic_relation_checkbox_32
    }
    Relation.Format.URL -> {
        if (isMedium)
            R.drawable.ic_relation_url_48
        else
            R.drawable.ic_relation_url_32
    }
    Relation.Format.EMAIL -> {
        if (isMedium)
            R.drawable.ic_relation_email_48
        else
            R.drawable.ic_relation_email_32
    }
    Relation.Format.PHONE -> {
        if (isMedium)
            R.drawable.ic_relation_phone_number_48
        else
            R.drawable.ic_relation_phone_number_32
    }
    Relation.Format.EMOJI -> R.drawable.ic_relation_object_32
    Relation.Format.OBJECT -> {
        if (isMedium)
            R.drawable.ic_relation_object_48
        else
            R.drawable.ic_relation_object_32
    }
    Relation.Format.TAG -> {
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

fun DVSortType.text(format: Relation.Format): Int = when (format) {
    Relation.Format.TAG, Relation.Format.STATUS -> {
        if (this == DVSortType.ASC)
            R.string.sort_from_first_to_last
        else
            R.string.sort_from_last_to_first
    }
    Relation.Format.NUMBER, Relation.Format.DATE -> {
        if (this == DVSortType.ASC)
            R.string.sort_from_one_to_nine
        else
            R.string.sort_from_nine_to_one
    }
    else -> {
        if (this == DVSortType.ASC)
            R.string.sort_from_a_to_z
        else
            R.string.sort_from_z_to_a
    }
}

fun String.getMimeIcon(): Int = when (MimeTypes.category(this)) {
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