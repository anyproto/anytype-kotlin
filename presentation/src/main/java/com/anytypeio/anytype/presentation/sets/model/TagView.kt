package com.anytypeio.anytype.presentation.sets.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TagView(val id: String, val tag: String, val color: String): Parcelable
@Parcelize
data class StatusView(val id: String, val status: String, val color: String) : Parcelable

@Parcelize
data class ObjectView(
    val id: String,
    val name: String,
    val emoji: String?,
    val image: String?
) : Parcelable

@Parcelize
data class FileView(
    val id: String,
    val ext: String,
    val mime: String,
    val name: String
) : Parcelable