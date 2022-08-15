package com.anytypeio.anytype.presentation.editor.markup

interface MarkupStyleDescriptor {

    val range: IntRange
    val isBold: Boolean
    val isItalic: Boolean
    val isStrikethrough: Boolean
    val isUnderline: Boolean
    val isCode: Boolean
    val isLinked: Boolean
    val markupTextColor: String?
    val markupUrl: String?
    val markupHighlightColor: String?

    val blockTextColor: String?
    val blockBackroundColor: String?

    data class Default(
        override val isBold: Boolean,
        override val isItalic: Boolean,
        override val isCode: Boolean,
        override val isStrikethrough: Boolean,
        override val isUnderline: Boolean,
        override val isLinked: Boolean,
        override val markupTextColor: String?,
        override val markupHighlightColor: String?,
        override val markupUrl: String?,
        override val blockTextColor: String?,
        override val blockBackroundColor: String?,
        override val range: IntRange
    ) : MarkupStyleDescriptor
}