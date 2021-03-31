package com.anytypeio.anytype.presentation.sets.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ColumnView(
    val key: String,
    val text: String,
    val format: Format,
    val width: Int,
    val isVisible: Boolean,
    val isHidden: Boolean,
    val isReadOnly: Boolean
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