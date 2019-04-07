package com.agileburo.anytype.feature_editor.factory

import com.agileburo.anytype.feature_editor.domain.*

object BlockFactory {

    fun makeBlock(
        contentType: ContentType = ContentType.P,
        contentParam: ContentParam = ContentParam.empty(),
        marks : List<Mark> = emptyList()
    ) : Block {
        return Block(
            id = DataFactory.randomString(),
            parentId = DataFactory.randomString(),
            contentType = contentType,
            content = Content.Text(
                text = DataFactory.randomString(),
                param = contentParam,
                marks = marks
            )
        )
    }

}