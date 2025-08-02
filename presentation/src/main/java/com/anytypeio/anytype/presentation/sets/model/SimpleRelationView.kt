package com.anytypeio.anytype.presentation.sets.model

import android.os.Parcelable
import com.anytypeio.anytype.core_models.RelationFormat
import kotlinx.parcelize.Parcelize

/**
 * @property isVisible - whether this relation is visible in viewer
 * @property isHidden - whether this relation is internal (not displayed to user at all)
 */
@Parcelize
data class SimpleRelationView(
    val key: String,
    val title: String,
    val format: RelationFormat,
    val isVisible: Boolean = false,
    val isHidden: Boolean = false,
    val isReadonly: Boolean = false,
    val isDefault: Boolean = false
) : Parcelable