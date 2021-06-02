package com.anytypeio.anytype.presentation.sets.model

import android.os.Parcelable
import com.anytypeio.anytype.core_models.Block
import kotlinx.android.parcel.Parcelize

@Deprecated("To be deleted")
@Parcelize
data class ColumnView(
    val key: String,
    val text: String,
    val format: Format,
    val width: Int,
    val isVisible: Boolean,
    val isHidden: Boolean,
    val isReadOnly: Boolean,
    val isDateIncludeTime: Boolean? = null,
    val dateFormat: Block.Content.DataView.DateFormat? = null,
    val timeFormat: Block.Content.DataView.TimeFormat? = null
) : Parcelable {

    @Parcelize
    enum class Format : Parcelable {
        SHORT_TEXT,
        LONG_TEXT,
        NUMBER,
        STATUS,
        DATE,
        FILE,
        CHECKBOX,
        URL,
        EMAIL,
        PHONE,
        EMOJI,
        OBJECT,
        TAG,
        RELATIONS
    }
}