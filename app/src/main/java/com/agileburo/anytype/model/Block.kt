package com.agileburo.anytype.model

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
    object HL : ContentType()
    object Toggle : ContentType()
    object Check : ContentType()
    object H4 : ContentType()
}

data class Block(
    val id: String = "",
    val parentId: String = "",
    val type: BlockType = BlockType.Editable,
    val contentType: ContentType = ContentType.H1,
    val content: String = "",
    val children: List<Block> = emptyList()
)