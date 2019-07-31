package com.agileburo.anytype.feature_editor.factory

import com.agileburo.anytype.feature_editor.data.BlockModel
import com.agileburo.anytype.feature_editor.domain.*
import com.google.gson.JsonObject

object BlockFactory {

    fun makeBlock(
        id : String = DataFactory.randomString(),
        parentId : String = DataFactory.randomString(),
        contentType: ContentType = ContentType.P,
        contentParam: ContentParam = ContentParam.empty(),
        blockType: BlockType = BlockType.Editable,
        marks : List<Mark> = emptyList()
    ) : Block {
        return Block(
            id = id,
            parentId = parentId,
            contentType = contentType,
            content = Content.Text(
                text = DataFactory.randomString(),
                param = contentParam,
                marks = marks
            ),
            blockType = blockType
        )
    }

    fun makeBlockModel(
        id: String = DataFactory.randomString(),
        parentId: String = DataFactory.randomString(),
        children: List<BlockModel> = emptyList(),
        contentType : Int = DataFactory.randomInt()
    ): BlockModel {
        return BlockModel(
            id = id,
            parentId = parentId,
            contentType = contentType,
            type = DataFactory.randomInt(),
            children = children,
            content = JsonObject()
        )
    }

}