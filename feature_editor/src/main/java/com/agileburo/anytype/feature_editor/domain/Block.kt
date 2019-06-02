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
    object Divider : BlockType()
    object Video : BlockType()
    object Image : BlockType()
    object Page : BlockType()
    object NewPage : BlockType()
    object BookMark : BlockType()
    object File : BlockType()
}

sealed class ContentType {
    object None : ContentType()
    object P : ContentType()
    object Code : ContentType()
    object H1 : ContentType()
    object H2 : ContentType()
    object H3 : ContentType()
    object NumberedList : ContentType()
    object UL : ContentType()
    object Quote : ContentType()
    object Toggle : ContentType()
    object Check : ContentType()
    object H4 : ContentType()
}

data class Block(
    val id: String,
    val parentId: String,
    var contentType: ContentType,
    val blockType: BlockType,
    val content: Content
) {

    fun setNumber(number: Int) {
        if (content is Content.Text)
            content.param.number = number
        else
            throw IllegalStateException("Could not set number because content was of type: ${content.javaClass.simpleName}")
    }


}