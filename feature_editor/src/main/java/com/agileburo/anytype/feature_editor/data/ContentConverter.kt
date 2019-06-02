package com.agileburo.anytype.feature_editor.data

import com.agileburo.anytype.feature_editor.domain.Content
import com.agileburo.anytype.feature_editor.domain.ContentParam

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 25.03.2019.
 */
interface ContentConverter {
    fun modelToDomain(model: ContentModel.Text): Content.Text
    fun domainToModel(domain: Content.Text): ContentModel.Text
    fun modelToDomain(model : ContentModel.Page) : Content.Page
    fun modelToDomain(model : ContentModel.Bookmark) : Content.Bookmark
    fun modelToDomain(model : ContentModel.Image) : Content.Picture
}

class ContentConverterImpl(private val markConverter: MarkConverter) : ContentConverter {

    override fun modelToDomain(model: ContentModel.Text) =
        Content.Text(
            text = model.text,
            marks = model.marks.map { markConverter.modelToDomain(it) },
            param = ContentParam(
                mutableMapOf(
                    "number" to (model.number ?: 0),
                    "checked" to (model.checked ?: false)
                )
            )
        )

    //TODO add marks convert!
    override fun domainToModel(domain: Content.Text) =
        ContentModel.Text(
            text = domain.text.toString(),
            marks = domain.marks.map { markConverter.domainToModel(it) },
            number = domain.param.number,
            checked = domain.param.checked
        )

    override fun modelToDomain(model: ContentModel.Page): Content.Page {
        return Content.Page(model.id)
    }

    override fun modelToDomain(model: ContentModel.Bookmark): Content.Bookmark {
        return Content.Bookmark(
            type = model.bookMark.type,
            description = model.bookMark.description,
            title = model.bookMark.title,
            url = model.bookMark.url,
            site = model.bookMark.site,
            icon = model.bookMark.icon,
            images = model.bookMark.images.map { image -> Content.Bookmark.Image(image.url) }
        )
    }

    override fun modelToDomain(model: ContentModel.Image): Content.Picture {
        // TODO remove hard-coded
        return Content.Picture(
            url = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/Francesco_Salviati_005.jpg/1280px-Francesco_Salviati_005.jpg",
            type = Content.Picture.Type.ORIGINAL
        )
    }
}