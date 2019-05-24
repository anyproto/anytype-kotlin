package com.agileburo.anytype.feature_editor.data

import com.agileburo.anytype.feature_editor.domain.Block
import com.agileburo.anytype.feature_editor.domain.BlockType
import com.agileburo.anytype.feature_editor.domain.toContentType
import com.agileburo.anytype.feature_editor.domain.toNumericalCode

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 20.03.2019.
 */
interface BlockConverter {
    fun modelToDomain(model: BlockModel): Block
    fun domainToModel(block: Block): BlockModel
}

class BlockConverterImpl(private val contentConverter: ContentConverter) : BlockConverter {

    override fun modelToDomain(model: BlockModel) = Block(
        id = model.id,
        content = contentConverter.modelToDomain(model.content),
        parentId = model.parentId,
        contentType = model.contentType.toContentType(),
        // TODO refactor
        blockType = BlockType.Editable
    )

    override fun domainToModel(block: Block) = BlockModel(
        id = block.id,
        content = contentConverter.domainToModel(block.content),
        parentId = block.parentId,
        children = mutableListOf(),
        contentType = block.contentType.toNumericalCode()
    )
}