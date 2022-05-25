package com.anytypeio.anytype.presentation.sets.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SortingExpression(
    val key: String,
    val type: Viewer.SortType = Viewer.SortType.ASC
) : Parcelable

