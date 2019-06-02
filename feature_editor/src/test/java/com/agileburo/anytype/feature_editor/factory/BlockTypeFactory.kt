package com.agileburo.anytype.feature_editor.factory

import com.agileburo.anytype.feature_editor.domain.BlockType

object BlockTypeFactory {

    fun values(): List<BlockType> {
        return listOf(
            BlockType.HrGrid,
            BlockType.VrGrid,
            BlockType.Editable,
            BlockType.Divider,
            BlockType.Video,
            BlockType.Image,
            BlockType.Page,
            BlockType.NewPage,
            BlockType.BookMark,
            BlockType.File
        )
    }

}
