package com.agileburo.anytype.feature_editor.factory

import com.agileburo.anytype.feature_editor.domain.ContentType

object ContentTypeFactory {

    fun values() : List<ContentType> {
        return listOf(
            ContentType.None,
            ContentType.P,
            ContentType.Code,
            ContentType.H1,
            ContentType.H2,
            ContentType.H3,
            ContentType.H4,
            ContentType.NumberedList,
            ContentType.UL,
            ContentType.Quote,
            ContentType.Toggle,
            ContentType.Check
        )
    }


}