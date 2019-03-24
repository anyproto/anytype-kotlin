package com.agileburo.anytype.feature_editor.presentation

import com.agileburo.anytype.feature_editor.domain.ContentType

interface BlockContentTypeConverter {

    fun getPermittedTypes(typeInitial: ContentType): Set<ContentType>
}

class BlockContentTypeConverterImpl : BlockContentTypeConverter {

    override
    fun getPermittedTypes(typeInitial: ContentType): Set<ContentType> =
        setOf(
            ContentType.P, ContentType.Code, ContentType.H1, ContentType.H2,
            ContentType.H3, ContentType.OL, ContentType.UL, ContentType.Quote,
            ContentType.Toggle, ContentType.Check, ContentType.H4
        )
}