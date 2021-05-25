package com.anytypeio.anytype.presentation.sets.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//todo Move to FilterView maybe
sealed class FilterValue : Parcelable {

    @Parcelize
    data class TextShort(val value: String?) : FilterValue() {
        fun isEmpty(): Boolean = this == empty()

        companion object {
            fun empty() = TextShort(null)
        }
    }

    @Parcelize
    data class Url(val value: String?) : FilterValue() {
        fun isEmpty(): Boolean = this == empty()

        companion object {
            fun empty() = Url(null)
        }
    }

    @Parcelize
    data class Email(val value: String?) : FilterValue() {
        fun isEmpty(): Boolean = this == empty()

        companion object {
            fun empty() = Email(null)
        }
    }

    @Parcelize
    data class Phone(val value: String?) : FilterValue() {
        fun isEmpty(): Boolean = this == empty()

        companion object {
            fun empty() = Phone(null)
        }
    }

    @Parcelize
    data class Text(val value: String?) : FilterValue() {
        fun isEmpty(): Boolean = this == empty()

        companion object {
            fun empty() = Text(null)
        }
    }

    @Parcelize
    data class Number(val value: String?) : FilterValue() {
        fun isEmpty(): Boolean = this == empty()

        companion object {
            fun empty() = Number(null)
        }
    }

    @Parcelize
    data class Date(val value: Long?) : FilterValue() {
        fun isEmpty(): Boolean = this.value == null || this == empty()

        companion object {
            fun empty() = Date(0L)
        }
    }

    @Parcelize
    data class Check(val value: Boolean?) : FilterValue() {
        fun isEmpty(): Boolean = this == empty()

        companion object {
            fun empty() = Check(null)
        }
    }

    @Parcelize
    data class Status(val value: StatusView?) : FilterValue() {
        fun isEmpty(): Boolean = this == empty()

        companion object {
            fun empty() = Status(null)
        }
    }

    @Parcelize
    data class Tag(val value: List<TagView>) : FilterValue() {
        fun isEmpty(): Boolean = this == empty()

        companion object {
            fun empty() = Tag(listOf())
        }
    }

    @Parcelize
    data class Object(val value: List<ObjectView>) : FilterValue() {
        fun isEmpty(): Boolean = this == empty()

        companion object {
            fun empty() = Object(listOf())
        }
    }
}

data class FilterExpression(
    val key: String,
    val operator: Viewer.FilterOperator,
    val condition: Viewer.Filter.Condition,
    val value: FilterValue?
)

data class FilterScreenData(
    val relations: List<SimpleRelationView>,
    val filters: List<FilterExpression>
) {
    companion object {
        fun empty() = FilterScreenData(emptyList(), emptyList())
    }
}

@Parcelize
data class TagValue(val id: String, val text: String, val color: String): Parcelable