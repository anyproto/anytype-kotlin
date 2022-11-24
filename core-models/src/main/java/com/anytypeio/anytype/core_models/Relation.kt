package com.anytypeio.anytype.core_models

/**
 * @property [format] format of the underlying data
 * @property [name] pretty name
 * @property [source] defines where the underlying data is stored
 * @property [isReadOnly] editable by user or not
 * @property [isHidden] whether this type is internal (not displayed to user)
 */
data class Relation(
    @Deprecated("Will be deprecated. Relations will be treated as objects")
    val key: String,
    @Deprecated("Will be deprecated. Relations will be treated as objects")
    val name: String,
    @Deprecated("Will be deprecated. Relations will be treated as objects")
    val format: Format,
    @Deprecated("Will be deprecated. Relations will be treated as objects")
    val source: Source,
    @Deprecated("Will be deprecated. Relations will be treated as objects")
    val isHidden: Boolean = false,
    @Deprecated("Will be deprecated. Relations will be treated as objects")
    val isReadOnly: Boolean = false,
    @Deprecated("Will be deprecated. Relations will be treated as objects")
    val isMulti: Boolean = false,
    @Deprecated("Will be deprecated. Relations will be treated as objects")
    val selections: List<Option> = emptyList(),
    @Deprecated("Will be deprecated. Relations will be treated as objects")
    val objectTypes: List<String> = emptyList(),
    @Deprecated("Will be deprecated. Relations will be treated as objects")
    val defaultValue: Any? = null
) {

    enum class Format(val code: Int) {
        LONG_TEXT(0),
        SHORT_TEXT(1),
        NUMBER(2),
        STATUS(3),
        TAG(11),
        DATE(4),
        FILE(5),
        CHECKBOX(6),
        URL(7),
        EMAIL(8),
        PHONE(9),
        EMOJI(10),
        OBJECT(100),
        RELATIONS(101),

        UNDEFINED(-1)
    }

    enum class Source {
        DETAILS, DERIVED, ACCOUNT, LOCAL
    }

    data class Option(
        val id: String,
        val text: String,
        val color: String
    )

    companion object {
        fun orderedFormatList(): List<Format> = listOf(
            Format.OBJECT,
            Format.LONG_TEXT,
            Format.NUMBER,
            Format.SHORT_TEXT,
            Format.STATUS,
            Format.TAG,
            Format.DATE,
            Format.FILE,
            Format.CHECKBOX,
            Format.URL,
            Format.EMAIL,
            Format.PHONE,
            Format.EMOJI,
            Format.RELATIONS
        )
    }
}