package com.anytypeio.anytype.presentation.sets.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @property isVisible - whether this relation is visible in viewer
 * @property isHidden - whether this relation is internal (not displayed to user at all)
 */
@Parcelize
data class SimpleRelationView(
    val key: String,
    val title: String,
    val format: ColumnView.Format,
    val isVisible: Boolean = false,
    val isHidden: Boolean = false
) : Parcelable