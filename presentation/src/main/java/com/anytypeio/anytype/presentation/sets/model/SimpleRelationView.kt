package com.anytypeio.anytype.presentation.sets.model

import android.os.Parcelable
import com.anytypeio.anytype.core_models.RelationFormat
import kotlinx.parcelize.Parcelize

/**
 * @property isVisible - whether this relation is visible in viewer
 * @property isHidden - whether this relation is internal (not displayed to user at all)
 * @property canToggleVisibility - whether the visibility toggle should be shown in Properties list.
 *           Set to false for relations like Name that are always visible and cannot be hidden.
 */
@Parcelize
data class SimpleRelationView(
    val key: String,
    val title: String,
    val format: RelationFormat,
    val isVisible: Boolean = false,
    val isHidden: Boolean = false,
    val isReadonly: Boolean = false,
    val isDefault: Boolean = false,
    val canToggleVisibility: Boolean = true
) : Parcelable