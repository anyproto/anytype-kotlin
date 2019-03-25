package com.agileburo.anytype.feature_editor.data

import com.agileburo.anytype.feature_editor.domain.Content

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
        Content.Text(text = model.text, marks = emptyList())

    //TODO add marks convert!
    override fun domainToModel(domain: Content.Text) =
        ContentModel(text = domain.text.toString(), marks = emptyList())
}