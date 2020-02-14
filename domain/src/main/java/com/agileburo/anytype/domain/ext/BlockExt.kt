package com.agileburo.anytype.domain.ext

import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Block.Content
import com.agileburo.anytype.domain.common.Id

/**
 * Maps blocks to its children using id as a key
 */
fun List<Block>.asMap(): Map<String, List<Block>> {
    val map: MutableMap<String, List<Block>> = mutableMapOf()
    forEach { block ->
        map[block.id] = block.children.mapNotNull { child -> find { it.id == child } }
    }
    return map
}

/**
 * Transform block structure for rendering purposes.
 * @param anchor a root or a parent block for some children blocks.
 */
fun Map<String, List<Block>>.asRender(anchor: String): List<Block> {
    val children = getValue(anchor)
    val result = mutableListOf<Block>()
    children.forEach { child ->
        if (child.content is Content.Text || child.content is Content.Image || child.content is Content.Link) {
            result.add(child)
            result.addAll(asRender(child.id))
        } else if (child.content is Content.Layout)
            result.addAll(asRender(child.id))
    }
    return result
}

/**
 * Tries to get block's content text style if possible.
 * @return block's text style
 * @throws UnsupportedOperationException if this block has no text content.
 */
fun Block.textStyle(): Content.Text.Style {
    if (content is Content.Text)
        return content.style
    else
        throw UnsupportedOperationException("Wrong block content type: ${content.javaClass}")
}

fun Content.Text.Mark.rangeIntersection(range: IntRange): Int {
    val markRange = IntRange(start = this.range.first, endInclusive = this.range.last)
    val set = markRange.intersect(range)
    return set.size
}

fun Block.getFirstLinkMarkupParam(range: IntRange): String? {
    val marks = this.content.asText().marks
    return marks.filter { mark -> mark.type == Block.Content.Text.Mark.Type.LINK }
        .firstOrNull { mark: Content.Text.Mark ->
            mark.rangeIntersection(range) > 0
        }.let { mark: Content.Text.Mark? -> mark?.param as? String }
}

fun Block.getSubstring(range: IntRange): String = content.asText().text.substring(range)

fun Block.textColor(): String? {
    if (content is Content.Text)
        return content.color
    else
        throw UnsupportedOperationException("Wrong block content type: ${content.javaClass}")
}


/**
 * Naive implementation for getting numbers from numbered-list blocks.
 * Does not work for nested numbered lists.
 * @return a map that maps ids to numbers.
 */
fun List<Block>.numbers(): Map<Id, Int> {

    val numbers = mutableMapOf<Id, Int>()

    var n = 0

    forEach { block ->
        val content = block.content

        n = if (content is Content.Text && content.style == Content.Text.Style.NUMBERED) {
            n.inc()
        } else {
            0
        }

        if (n != 0) numbers[block.id] = n
    }

    return numbers
}

inline fun <reified T> Block.content(): T {
    return content as T
}