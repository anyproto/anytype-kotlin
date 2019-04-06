package com.agileburo.anytype.feature_editor.presentation.converter

import com.agileburo.anytype.feature_editor.domain.Block
import com.agileburo.anytype.feature_editor.domain.ContentParam
import com.agileburo.anytype.feature_editor.domain.ContentType

interface BlockContentTypeConverter {

    /**
     * @param block block to convert
     * @param type content type for new block
     */
    fun convert(block : Block, type : ContentType) : Block

    /**
     * @param blocks list of blocks
     * @param target block that we need to convert
     * @param targetType type for a new block
     */
    fun convert(blocks : List<Block>, target : Block, targetType : ContentType) : List<Block>

    fun getPermittedTypes(typeInitial: ContentType): Set<ContentType>
    fun getForbiddenTypes(typeInitial: ContentType): Set<ContentType>
}

class BlockContentTypeConverterImpl :
    BlockContentTypeConverter {

    override
    fun getPermittedTypes(typeInitial: ContentType): Set<ContentType> =
        setOf(
            ContentType.P, ContentType.Code, ContentType.H1, ContentType.H2,
            ContentType.H3, ContentType.NumberedList, ContentType.UL, ContentType.Quote,
            ContentType.Toggle, ContentType.Check, ContentType.H4
        )

    //Если вдруг появятся недопустимые варианты для конвертации, добавлять можно здесь
    override fun getForbiddenTypes(typeInitial: ContentType): Set<ContentType> =
        when (typeInitial) {
            //Выключаем H1 при работе с блоком, используем H2, H2, H4
            ContentType.P -> setOf(ContentType.H1)
            else -> setOf(ContentType.H1)
        }

    override fun convert(block: Block, type: ContentType): Block {
        return when(type) {
            ContentType.NumberedList -> {
                block.copy(
                    contentType = type,
                    content = block.content.copy(
                        param = ContentParam.numberedList()
                    )
                )
            }
            else -> {
                block.copy(
                    contentType = type
                )
            }
        }
    }

    override fun convert(blocks: List<Block>, target: Block, targetType: ContentType): List<Block> {

        if (target.contentType == targetType)
            return blocks
        else
            when(targetType) {
                
                ContentType.NumberedList -> {

                    val result = mutableListOf<Block>()

                    blocks.forEach { block ->
                        if (block.id == target.id) {
                            val item = block.copy(
                                contentType = targetType,
                                content = block.content.copy(
                                    param = ContentParam.numberedList()
                                )
                            )
                            result.add(item)
                        } else {
                            result.add(block)
                        }
                    }
                    
                    return normalizeNumbers(result)
                }
                
                else -> {

                    val result = blocks.toMutableList().also { result ->
                        val index = blocks.indexOf(target)
                        val converted = target.copy(contentType = targetType)
                        result[index] = converted
                    }

                    return normalizeNumbers(result)

                }
            }
    }

    private fun normalizeNumbers(blocks : MutableList<Block>) : List<Block> {

        if (blocks.isEmpty())
            return blocks

        var number = 0
        var isPreviousNumbered = false

        blocks.forEach { block ->
            if (block.contentType == ContentType.NumberedList) {

                if (isPreviousNumbered) {
                    number++
                    block.setNumber(number)
                } else {
                    number = 1
                    block.setNumber(number)
                }

                isPreviousNumbered = true

            } else {
                block.setNumber(0)
                isPreviousNumbered = false
            }

        }

        return blocks

    }


}