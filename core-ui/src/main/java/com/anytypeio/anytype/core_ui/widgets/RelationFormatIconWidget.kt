package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import timber.log.Timber

class RelationFormatIconWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {
    fun bind(format: Relation.Format) {
        when (format) {
            Relation.Format.SHORT_TEXT -> setImageResource(R.drawable.ic_relation_format_text_small)
            Relation.Format.LONG_TEXT -> setImageResource(R.drawable.ic_relation_format_text_small)
            Relation.Format.NUMBER -> setImageResource(R.drawable.ic_relation_format_number_small)
            Relation.Format.STATUS -> setImageResource(R.drawable.ic_relation_format_status_small)
            Relation.Format.TAG -> setImageResource(R.drawable.ic_relation_format_tag_small)
            Relation.Format.DATE -> setImageResource(R.drawable.ic_relation_format_date_small)
            Relation.Format.FILE -> setImageResource(R.drawable.ic_relation_format_attachment_small)
            Relation.Format.CHECKBOX -> setImageResource(R.drawable.ic_relation_format_checkbox_small)
            Relation.Format.URL -> setImageResource(R.drawable.ic_relation_format_url_small)
            Relation.Format.EMAIL -> setImageResource(R.drawable.ic_relation_format_email_small)
            Relation.Format.PHONE -> setImageResource(R.drawable.ic_relation_format_phone_number_small)
            Relation.Format.OBJECT -> setImageResource(R.drawable.ic_relation_format_object_small)
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