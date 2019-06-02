package com.agileburo.anytype.feature_editor.data.converter

import com.agileburo.anytype.feature_editor.domain.BlockType

fun Int.toBlockType(): BlockType =
    when (this) {
        1 -> BlockType.HrGrid
        2 -> BlockType.VrGrid
        3 -> BlockType.Editable
        4 -> BlockType.Divider
        5 -> BlockType.Video
        6 -> BlockType.Image
        7 -> BlockType.Page
        8 -> BlockType.NewPage
        9 -> BlockType.BookMark
        10 -> BlockType.File
        else -> throw IllegalStateException("Unexpected block type code: $this")
    }

fun BlockType.toNumericalCode() : Int =
    when(this) {
        BlockType.HrGrid -> 1
        BlockType.VrGrid -> 2
        BlockType.Editable -> 3
        BlockType.Divider -> 4
        BlockType.Video -> 5
        BlockType.Image -> 6
        BlockType.Page -> 7
        BlockType.NewPage -> 8
        BlockType.BookMark -> 9
        BlockType.File -> 10
    }
