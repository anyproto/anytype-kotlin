package com.agileburo.anytype.feature_editor.data.converter

import com.agileburo.anytype.feature_editor.domain.ContentType

fun Int.toContentType(): ContentType =
    when (this) {
        0 -> ContentType.None
        1 -> ContentType.P
        2 -> ContentType.Code
        3 -> ContentType.H1
        4 -> ContentType.H2
        5 -> ContentType.H3
        6 -> ContentType.NumberedList
        7 -> ContentType.UL
        8 -> ContentType.Quote
        9 -> ContentType.Toggle
        10 -> ContentType.Check
        11 -> ContentType.H4
        else -> throw IllegalStateException("Unexpected content type code: $this")
    }

fun ContentType.toNumericalCode(): Int =
    when (this) {
        ContentType.None -> 0
        ContentType.P -> 1
        ContentType.Code -> 2
        ContentType.H1 -> 3
        ContentType.H2 -> 4
        ContentType.H3 -> 5
        ContentType.NumberedList -> 6
        ContentType.UL -> 7
        ContentType.Quote -> 8
        ContentType.Toggle -> 9
        ContentType.Check -> 10
        ContentType.H4 -> 11
    }
