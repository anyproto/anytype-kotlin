package com.agileburo.anytype.domain.ext

import com.agileburo.anytype.domain.block.model.Block

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
        if (child.content is Block.Content.Text || child.content is Block.Content.Image) {
            result.add(child)
            result.addAll(asRender(child.id))
        } else if (child.content is Block.Content.Layout)
            result.addAll(asRender(child.id))
    }
    return result
}
