package com.agileburo.anytype.feature_editor.data.converter

import com.agileburo.anytype.feature_editor.domain.BlockType
import com.agileburo.anytype.feature_editor.domain.BlockTypes

fun Int.toBlockType(): BlockType =
    when (this) {
        BlockTypes.HORIZONTAL_GRID -> BlockType.HrGrid
        BlockTypes.VERTICAL_GRID -> BlockType.VrGrid
        BlockTypes.EDITABLE -> BlockType.Editable
        BlockTypes.DIVIDER -> BlockType.Divider
        BlockTypes.VIDEO -> BlockType.Video
        BlockTypes.IMAGE -> BlockType.Image
        BlockTypes.PAGE -> BlockType.Page
        BlockTypes.NEW_PAGE -> BlockType.NewPage
        BlockTypes.BOOKMARK -> BlockType.BookMark
        BlockTypes.FILE -> BlockType.File
        else -> throw IllegalStateException("Unexpected block type code: $this")
    }

fun BlockType.toNumericalCode() : Int =
    when(this) {
        BlockType.HrGrid -> BlockTypes.HORIZONTAL_GRID
        BlockType.VrGrid -> BlockTypes.VERTICAL_GRID
        BlockType.Editable -> BlockTypes.EDITABLE
        BlockType.Divider -> BlockTypes.DIVIDER
        BlockType.Video -> BlockTypes.VIDEO
        BlockType.Image -> BlockTypes.IMAGE
        BlockType.Page -> BlockTypes.PAGE
        BlockType.NewPage -> BlockTypes.NEW_PAGE
        BlockType.BookMark -> BlockTypes.BOOKMARK
        BlockType.File -> BlockTypes.FILE
    }
