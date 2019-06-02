package com.agileburo.anytype.feature_editor.data

import com.agileburo.anytype.feature_editor.data.converter.toBlockType
import com.agileburo.anytype.feature_editor.data.converter.toContentType
import com.agileburo.anytype.feature_editor.data.parser.ContentModelParser
import com.agileburo.anytype.feature_editor.domain.Block
import com.agileburo.anytype.feature_editor.domain.BlockType
import com.agileburo.anytype.feature_editor.domain.Content
import com.agileburo.anytype.feature_editor.domain.ContentType


/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 20.03.2019.
 */
interface BlockConverter {
    fun modelToDomain(model: BlockModel): Block
}

class BlockConverterImpl(
    private val contentConverter: ContentConverter,
    private val contentModelParser: ContentModelParser
) : BlockConverter {

    override fun modelToDomain(model: BlockModel) : Block {

        val type = model.type.toBlockType()

        when (type) {
            is BlockType.Editable -> {

                val parsed = contentModelParser.parse(model.content, type)

                val content = contentConverter.modelToDomain(parsed as ContentModel.Text)

                return Block(
                    id = model.id,
                    content = content,
                    parentId = model.parentId,
                    contentType = model.contentType.toContentType(),
                    blockType = model.type.toBlockType()
                )

            }
            is BlockType.Page -> {

                val parsed = contentModelParser.parse(model.content, type)

                val content = contentConverter.modelToDomain(parsed as ContentModel.Page)

                return Block(
                    id = model.id,
                    content = content,
                    parentId = model.parentId,
                    contentType = model.contentType.toContentType(),
                    blockType = model.type.toBlockType()
                )

            }

            is BlockType.BookMark -> {

                val parsed = contentModelParser.parse(model.content, type)

                val content = contentConverter.modelToDomain(parsed as ContentModel.Bookmark)

                return Block(
                    id = model.id,
                    content = content,
                    parentId = model.parentId,
                    contentType = model.contentType.toContentType(),
                    blockType = model.type.toBlockType()
                )

            }

            is BlockType.Divider -> {

                return Block(
                    id = model.id,
                    parentId = model.parentId,
                    contentType = ContentType.None,
                    content = Content.Empty,
                    blockType = BlockType.Divider
                )

            }

            is BlockType.Image -> {

                val parsed = contentModelParser.parse(model.content, type) as ContentModel.Image
                val content = contentConverter.modelToDomain(parsed)

                return Block(
                    id = model.id,
                    parentId = model.parentId,
                    content = content,
                    contentType = ContentType.None,
                    blockType = BlockType.Image
                )
            }

            else -> TODO()
        }
    }

}