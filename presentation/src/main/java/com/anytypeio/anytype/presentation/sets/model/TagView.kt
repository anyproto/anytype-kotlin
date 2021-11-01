package com.anytypeio.anytype.presentation.sets.model

import android.os.Parcelable
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TagView(val id: String, val tag: String, val color: String) : Parcelable

@Parcelize
data class StatusView(val id: String, val status: String, val color: String) : Parcelable


sealed class ObjectView {

    abstract val id: Id

    data class Default(
        override val id: String,
        val name: String,
        val icon: ObjectIcon,
        val types: List<String>
    ) : ObjectView()

    data class Deleted(override val id: String) : ObjectView()
}

@Parcelize
data class FileView(
    val id: String,
    val ext: String,
    val mime: String,
    val name: String
) : Parcelable