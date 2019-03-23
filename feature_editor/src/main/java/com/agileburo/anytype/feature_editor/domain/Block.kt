package com.agileburo.anytype.feature_editor.domain

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 14.03.2019.
 */
sealed class BlockType {
    object HrGrid : BlockType()
    object VrGrid : BlockType()
    object Editable : BlockType()
    object Div : BlockType()
    object YouTube : BlockType()
    object Image : BlockType()
    object Page : BlockType()
    object NewPage : BlockType()
    object BookMark : BlockType()
    object File : BlockType()
}

sealed class ContentType {
    object P : ContentType()
    object Code : ContentType()
    object H1 : ContentType()
    object H2 : ContentType()
    object H3 : ContentType()
    object OL : ContentType()
    object UL : ContentType()
    object Quote : ContentType()
    object Toggle : ContentType()
    object Check : ContentType()
    object H4 : ContentType()
}

data class Block(
    val id: String,
    val parentId: String,
    val contentType: ContentType,
    // TODO parse marks and other stuff
    val content: String
    // TODO add blockType
)

fun Int.toContentType(): ContentType =
    when (this) {
        1 -> ContentType.P
        2 -> ContentType.Code
        3 -> ContentType.H1
        4 -> ContentType.H2
        5 -> ContentType.H3
        6 -> ContentType.OL
        7 -> ContentType.UL
        8 -> ContentType.Quote
        9 -> ContentType.Toggle
        10 -> ContentType.Check
        11 -> ContentType.H4
        else -> ContentType.H1
    }

fun ContentType.toNumericalCode() : Int {
    return when(this) {
        ContentType.P -> 1
        ContentType.H1 -> 3
        ContentType.H2 -> 4
        ContentType.Quote -> 8
        else -> TODO()
    }
}

fun Int.toBlockType(): BlockType =
    when (this) {
        1 -> BlockType.HrGrid
        2 -> BlockType.VrGrid
        3 -> BlockType.Editable
        4 -> BlockType.Div
        5 -> BlockType.YouTube
        6 -> BlockType.Image
        7 -> BlockType.Page
        8 -> BlockType.NewPage
        9 -> BlockType.BookMark
        10 -> BlockType.File
        else -> BlockType.Editable
    }