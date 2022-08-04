package com.anytypeio.anytype.core_models

/**
 * @property [format] format of the underlying data
 * @property [name] pretty name
 * @property [source] defines where the underlying data is stored
 * @property [isReadOnly] editable by user or not
 * @property [isHidden] whether this type is internal (not displayed to user)
 */
@Deprecated("Will be deprecated. Relations will be treated as objects")
data class Relation(
    val key: String,
    val name: String,
    val format: Format,
    val source: Source,
    val isHidden: Boolean = false,
    val isReadOnly: Boolean = false,
    val isMulti: Boolean = false,
    val selections: List<Option> = emptyList(),
    val objectTypes: List<String> = emptyList(),
    val defaultValue: Any? = null
) {

    enum class Format(val prettyName: String) {
        LONG_TEXT("Text"),
        SHORT_TEXT("Short text"),
        NUMBER("Number"),
        STATUS("Status"),
        TAG("Tag"),
        DATE("Date"),
        FILE("File & Media"),
        CHECKBOX("Checkbox"),
        URL("URL"),
        EMAIL("Email"),
        PHONE("Phone"),
        EMOJI("Emoji"),
        OBJECT("Object"),
        RELATIONS("Relations")
    }

    enum class Source {
        DETAILS, DERIVED, ACCOUNT, LOCAL
    }

    data class Option(
        val id: String,
        val text: String,
        val color: String,
        val scope: OptionScope = OptionScope.LOCAL
    )

    enum class OptionScope {
        LOCAL, RELATION, FORMAT
    }

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