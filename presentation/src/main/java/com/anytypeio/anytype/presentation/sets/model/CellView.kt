package com.anytypeio.anytype.presentation.sets.model

sealed class CellView {

    abstract val key: String
    abstract val id: String

    interface Text {
        val text: String
    }

    interface Numbered {
        val number: String?
    }

    interface DateFormat {
        val dateFormat: String
    }

    data class Description(
        override val id: String,
        override val key: String,
        override val text: String
    ) : CellView(), Text

    data class Date(
        override val id: String,
        override val key: String,
        val timeInMillis: Long? = null,
        override val dateFormat: String,
    ) : CellView(), DateFormat

    data class Number(
        override val id: String,
        override val key: String,
        override val number: String?
    ) : CellView(), Numbered

    data class Url(
        override val id: String,
        override val key: String,
        val url: String?
    ) : CellView()

    data class Email(
        override val id: String,
        override val key: String,
        val email: String?
    ) : CellView()

    data class Phone(
        override val id: String,
        override val key: String,
        val phone: String?
    ) : CellView()

    data class Tag(
        override val id: String,
        override val key: String,
        val tags: List<TagView>
    ) : CellView()

    data class Status(
        override val id: String,
        override val key: String,
        val status: List<StatusView>
    ) : CellView()

    data class Object(
        override val id: String,
        override val key: String,
        val objects: List<ObjectView>
    ) : CellView()

    data class Checkbox(
        override val id: String,
        override val key: String,
        val isChecked: Boolean
    ): CellView()

    data class File(
        override val id: String,
        override val key: String,
        val files: List<FileView>
    ) : CellView()
}