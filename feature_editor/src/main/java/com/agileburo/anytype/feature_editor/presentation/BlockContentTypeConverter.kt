package com.agileburo.anytype.feature_editor.presentation

import com.agileburo.anytype.feature_editor.domain.ContentType

interface BlockContentTypeConverter {

    fun getPermittedTypes(typeInitial: ContentType): Set<ContentType>
    fun getForbiddenTypes(typeInitial: ContentType): Set<ContentType>
}

class BlockContentTypeConverterImpl : BlockContentTypeConverter {

    override
    fun getPermittedTypes(typeInitial: ContentType): Set<ContentType> =
        setOf(
            ContentType.P, ContentType.Code, ContentType.H1, ContentType.H2,
            ContentType.H3, ContentType.OL, ContentType.UL, ContentType.Quote,
            ContentType.Toggle, ContentType.Check, ContentType.H4
        )

    //Если вдруг появятся недопустимые варианты для конвертации, добавлять можно здесь
    override fun getForbiddenTypes(typeInitial: ContentType): Set<ContentType> =
        when (typeInitial) {
            //Выключаем H1 при работе с блоком, используем H2, H2, H4
            ContentType.P -> setOf(ContentType.H1)
            else -> setOf(ContentType.H1)
        }
}