package com.agileburo.anytype.feature_editor.data.converter

import com.agileburo.anytype.feature_editor.domain.ContentType
import com.agileburo.anytype.feature_editor.domain.ContentTypes

fun Int.toContentType(): ContentType =
    when (this) {
        ContentTypes.UNDEFINED -> ContentType.None
        ContentTypes.PARAGRAPH -> ContentType.P
        ContentTypes.CODE_SNIPPET -> ContentType.Code
        ContentTypes.HEADER_ONE -> ContentType.H1
        ContentTypes.HEADER_TWO -> ContentType.H2
        ContentTypes.HEADER_THREE -> ContentType.H3
        ContentTypes.NUMBERED_LIST -> ContentType.NumberedList
        ContentTypes.BULLET_LIST_ITEM -> ContentType.UL
        ContentTypes.QUOTE -> ContentType.Quote
        ContentTypes.TOGGLE -> ContentType.Toggle
        ContentTypes.CHECKBOX -> ContentType.Check
        ContentTypes.HEADER_FOUR -> ContentType.H4
        else -> throw IllegalStateException("Unexpected content type code: $this")
    }

fun ContentType.toNumericalCode(): Int =
    when (this) {
        ContentType.None -> ContentTypes.UNDEFINED
        ContentType.P -> ContentTypes.PARAGRAPH
        ContentType.Code -> ContentTypes.CODE_SNIPPET
        ContentType.H1 -> ContentTypes.HEADER_ONE
        ContentType.H2 -> ContentTypes.HEADER_TWO
        ContentType.H3 -> ContentTypes.HEADER_THREE
        ContentType.NumberedList -> ContentTypes.NUMBERED_LIST
        ContentType.UL -> ContentTypes.BULLET_LIST_ITEM
        ContentType.Quote -> ContentTypes.QUOTE
        ContentType.Toggle -> ContentTypes.TOGGLE
        ContentType.Check -> ContentTypes.CHECKBOX
        ContentType.H4 -> ContentTypes.HEADER_FOUR
    }
