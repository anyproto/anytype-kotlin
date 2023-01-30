package com.anytypeio.anytype.presentation.sets.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class FilterValue {

    data class TextShort(val value: String?) : FilterValue() {
        fun isEmpty(): Boolean = this == empty()

        companion object {
            fun empty() = TextShort(null)
        }
    }

    data class Url(val value: String?) : FilterValue() {
        fun isEmpty(): Boolean = this == empty()

        companion object {
            fun empty() = Url(null)
        }
    }

    data class Email(val value: String?) : FilterValue() {
        fun isEmpty(): Boolean = this == empty()

        companion object {
            fun empty() = Email(null)
        }
    }

    data class Phone(val value: String?) : FilterValue() {
        fun isEmpty(): Boolean = this == empty()

        companion object {
            fun empty() = Phone(null)
        }
    }

    data class Text(val value: String?) : FilterValue() {
        fun isEmpty(): Boolean = this == empty()

        companion object {
            fun empty() = Text(null)
        }
    }

    data class Number(val value: String?) : FilterValue() {
        fun isEmpty(): Boolean = this == empty()

        companion object {
            fun empty() = Number(null)
        }
    }

    data class Date(val value: Long?) : FilterValue() {
        fun isEmpty(): Boolean = this.value == null || this == empty()

        companion object {
            fun empty() = Date(0L)
        }
    }

    data class Check(val value: Boolean?) : FilterValue() {
        fun isEmpty(): Boolean = this == empty()

        companion object {
            fun empty() = Check(null)
        }
    }

    data class Status(val value: StatusView?) : FilterValue() {
        fun isEmpty(): Boolean = this == empty()

        companion object {
            fun empty() = Status(null)
        }
    }

    data class Tag(val value: List<TagView>) : FilterValue() {
        fun isEmpty(): Boolean = this == empty()

        companion object {
            fun empty() = Tag(listOf())
        }
    }

    data class Object(val value: List<ObjectView>) : FilterValue() {
        fun isEmpty(): Boolean = this == empty()

        companion object {
            fun empty() = Object(listOf())
        }
    }
}

@Parcelize
data class TagValue(val id: String, val text: String, val color: String): Parcelable