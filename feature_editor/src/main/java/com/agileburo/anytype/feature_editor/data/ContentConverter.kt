package com.agileburo.anytype.feature_editor.data

import com.agileburo.anytype.feature_editor.domain.Content
import com.agileburo.anytype.feature_editor.domain.ContentParam

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 25.03.2019.
 */
interface ContentConverter {
    fun modelToDomain(model: ContentModel): Content.Text
    fun domainToModel(domain: Content.Text): ContentModel
}

class ContentConverterImpl : ContentConverter {

    //TODO add marks convert!
    override fun modelToDomain(model: ContentModel) =
        Content.Text(
            text = model.text,
            marks = emptyList(),
            param = ContentParam(
                mutableMapOf(
                    "number" to (model.number ?: 0),
                    "checked" to (model.checked ?: false)
                )
            )
        )

    //TODO add marks convert!
    override fun domainToModel(domain: Content.Text) =
        ContentModel(
            text = domain.text.toString(),
            marks = domain.marks.map { it.toString() },
            number = domain.param.number,
            checked = domain.param.checked
        )
}