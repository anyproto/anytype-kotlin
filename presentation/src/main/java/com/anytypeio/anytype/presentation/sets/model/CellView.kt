package com.anytypeio.anytype.presentation.sets.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class CellView : Parcelable {

    abstract val key: String
    abstract val id: String

    interface Text {
        val text: String
    }

    interface Numbered {
        val number: Int?
    }

    interface Icon {
        val icon: String
    }

    interface DateFormat {
        val dateFormat: String
    }

    @Parcelize
    data class Title(
        override val id: String,
        override val key: String,
        override val text: String,
        override val icon: String
    ) : CellView(), Text, Icon, Parcelable

    @Parcelize
    data class Description(
        override val id: String,
        override val key: String,
        override val text: String
    ) : CellView(), Text, Parcelable

    @Parcelize
    data class Date(
        override val id: String,
        override val key: String,
        override val text: String,
        val timestamp: Long? = null,
        override val dateFormat: String,
    ) : CellView(), Text, DateFormat, Parcelable

    @Parcelize
    data class Number(
        override val id: String,
        override val key: String,
        override val number: Int?
    ) : CellView(), Numbered, Parcelable

    @Parcelize
    data class Url(
        override val id: String,
        override val key: String,
        val url: String?
    ) : CellView(), Parcelable

    @Parcelize
    data class Email(
        override val id: String,
        override val key: String,
        val email: String?
    ) : CellView(), Parcelable

    @Parcelize
    data class Phone(
        override val id: String,
        override val key: String,
        val phone: String?
    ) : CellView(), Parcelable

    @Parcelize
    data class Tag(
        override val id: String,
        override val key: String,
        val tags: List<TagView>
    ) : CellView(), Parcelable

    @Parcelize
    data class Status(
        override val id: String,
        override val key: String,
        val status: List<StatusView>
    ) : CellView(), Parcelable

    @Parcelize
    data class Object(
        override val id: String,
        override val key: String,
        val objects: List<ObjectView>
    ) : CellView(), Parcelable

    @Parcelize
    data class Checkbox(
        override val id: String,
        override val key: String,
        val isChecked: Boolean
    ): CellView(), Parcelable

    @Parcelize
    data class File(
        override val id: String,
        override val key: String,
        val files: List<FileView>
    ) : CellView(), Parcelable
}