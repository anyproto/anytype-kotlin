package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import timber.log.Timber

class RelationFormatIconWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {
    fun bind(format: RelationFormat) {
        when (format) {
            RelationFormat.SHORT_TEXT -> setImageResource(R.drawable.ic_relation_format_text_small)
            RelationFormat.LONG_TEXT -> setImageResource(R.drawable.ic_relation_format_text_small)
            RelationFormat.NUMBER -> setImageResource(R.drawable.ic_relation_format_number_small)
            RelationFormat.STATUS -> setImageResource(R.drawable.ic_relation_format_status_small)
            RelationFormat.TAG -> setImageResource(R.drawable.ic_relation_format_tag_small)
            RelationFormat.DATE -> setImageResource(R.drawable.ic_relation_format_date_small)
            RelationFormat.FILE -> setImageResource(R.drawable.ic_relation_format_attachment_small)
            RelationFormat.CHECKBOX -> setImageResource(R.drawable.ic_relation_format_checkbox_small)
            RelationFormat.URL -> setImageResource(R.drawable.ic_relation_format_url_small)
            RelationFormat.EMAIL -> setImageResource(R.drawable.ic_relation_format_email_small)
            RelationFormat.PHONE -> setImageResource(R.drawable.ic_relation_format_phone_number_small)
            RelationFormat.OBJECT -> setImageResource(R.drawable.ic_relation_format_object_small)
            else -> Timber.d("Unexpected format: $format")
        }
    }
    fun bind(format: ColumnView.Format) {
        when (format) {
            ColumnView.Format.SHORT_TEXT -> setImageResource(R.drawable.ic_relation_format_text_small)
            ColumnView.Format.LONG_TEXT -> setImageResource(R.drawable.ic_relation_format_text_small)
            ColumnView.Format.NUMBER -> setImageResource(R.drawable.ic_relation_format_number_small)
            ColumnView.Format.STATUS -> setImageResource(R.drawable.ic_relation_format_status_small)
            ColumnView.Format.TAG -> setImageResource(R.drawable.ic_relation_format_tag_small)
            ColumnView.Format.DATE -> setImageResource(R.drawable.ic_relation_format_date_small)
            ColumnView.Format.FILE -> setImageResource(R.drawable.ic_relation_format_attachment_small)
            ColumnView.Format.CHECKBOX -> setImageResource(R.drawable.ic_relation_format_checkbox_small)
            ColumnView.Format.URL -> setImageResource(R.drawable.ic_relation_format_url_small)
            ColumnView.Format.EMAIL -> setImageResource(R.drawable.ic_relation_format_email_small)
            ColumnView.Format.PHONE -> setImageResource(R.drawable.ic_relation_format_phone_number_small)
            ColumnView.Format.OBJECT -> setImageResource(R.drawable.ic_relation_format_object_small)
            else -> Timber.d("Unexpected format: $format")
        }
    }
}