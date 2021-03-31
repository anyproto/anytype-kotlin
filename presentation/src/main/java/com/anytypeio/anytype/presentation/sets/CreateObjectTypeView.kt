package com.anytypeio.anytype.presentation.sets

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CreateObjectTypeView(
    val name: String,
    val layout: Int,
    val isSelected: Boolean = false
) : Parcelable