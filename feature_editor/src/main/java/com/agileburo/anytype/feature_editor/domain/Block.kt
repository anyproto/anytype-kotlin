package com.agileburo.anytype.feature_editor.domain

import com.agileburo.anytype.feature_editor.presentation.mapper.BlockViewMapper
import com.agileburo.anytype.feature_editor.presentation.model.BlockView
import java.util.*

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

typealias Document = MutableList<Block>

data class Block(
    val id: String,
    val parentId: String,
    val contentType: ContentType,
    val blockType: BlockType,
    val content: Content,
    val children : MutableList<Block> = mutableListOf(),
    val state : State = State.expanded(false)
) {

    fun isNumberedList() = contentType == ContentType.NumberedList

    fun setNumber(number: Int) {
        if (content is Content.Text) content.param.number = number
    }

    fun isConsumer(): Boolean = when (contentType) {
        ContentType.Toggle, ContentType.Check, ContentType.NumberedList, ContentType.UL -> true
        else -> false
    }

    fun hasParent() = parentId.isNotEmpty()

    fun isList() : Boolean {
        return when(contentType) {
            ContentType.NumberedList, ContentType.UL, ContentType.Check -> true
            else -> false
        }
    }

    fun isCheckbox() = contentType == ContentType.Check

    fun isToggle() = contentType == ContentType.Toggle

    data class State(val map : MutableMap<String, Any> = mutableMapOf()) {
        var expanded : Boolean by map
        var focused : Boolean by map

        companion object {
            fun expanded(expanded : Boolean = false) = State(mutableMapOf("expanded" to expanded, "focused" to false))
            fun focused(focused : Boolean = false) = State(mutableMapOf("focused" to focused))
        }
    }

    companion object {
        fun new(parentId: String, contentType : ContentType) : Block {
            return Block(
                id = UUID.randomUUID().toString(),
                parentId = parentId,
                content = Content.Text(
                    marks = emptyList(),
                    text = "",
                    param = ContentParam.empty()
                ),
                blockType = BlockType.Editable,
                contentType = contentType,
                state = State.focused(true)
            )
        }
    }

}

/**
 * Flattens document, so that as result we get a flattened list.
 * @return list, in which all blocks do not have children (parent-child relations are indicated only via parentId)
 */
fun Document.flat() : List<Block> {
    val result = mutableListOf<Block>()

    forEach { block ->
        result.add(block.copy(children = mutableListOf()))
        if (block.children.isNotEmpty()) result.addAll(block.children.flat())
    }

    return result
}

/**
 * This method allows to transform a flattened list of block into a graph-like structured document.
 */
fun Document.graph() : Document {
    val map = this.associateBy { block -> block.id }.toMutableMap()

    forEach { block ->
        if (block.parentId.isNotEmpty()) {
             map[block.parentId]?.let { parent ->
                 parent.children.add(block)
                 map[block.parentId] = parent
                 map[block.id] = block
            }
        }
    }

    return map.values.filter { block -> block.parentId.isEmpty() }.toMutableList()
}

/**
 * Search inside a flattened block list.
 */
fun Document.flatSearch(id : String) : Block? {
    return flat().find { block -> block.id == id }?.copy()
}

/**
 * Search inside a graph-like structured document.
 * @param id id of the block to search
 * @return returns a copy instance of the block, or null if it is not present inside the document
 */
fun Document.search(id : String) : Block? {
    forEach { block ->
        if (block.id == id) return block.copy()

        if (block.children.isNotEmpty()) {
            val result = block.children.search(id)
            if (result != null) return result.copy()
        }
    }

    return null
}

/**
 * @param targetId id of the block to update.
 * @param targetType new content type for the block that is being updated
 */
@Throws(IllegalStateException::class)
fun Document.changeContentType(targetId : String, targetType: ContentType) {

    search(targetId)?.let { target ->

        if (target.contentType == targetType) return

        if (target.parentId.isNotEmpty()) {

            // Changing content type at children level

            search(target.parentId)?.let { parent ->

                val index = parent.children.indexOf(target)

                parent.children[index] = target.copy(contentType = targetType)

                // treat toggle block children move.

                if (target.contentType == ContentType.Toggle) {

                    // move toggle block children to an upper level

                    if (target.children.isNotEmpty()) {

                        val children = target.children.map { it.copy(parentId = parent.id) }

                        // remove children from toggle block

                        target.children.clear()

                        // add children to their new parent (i.e. parent of the target block)

                        if (index < parent.children.size - 1) {
                            val slice = slice(index + 1 until parent.children.size)
                            parent.children.removeAll { child -> slice.contains(child) }
                            parent.children.addAll(children + slice)
                        } else {
                            parent.children.addAll(children)
                        }
                    }
                }
            } ?: throw IllegalStateException("Could not found parent by id : ${target.parentId}")

        } else {

            // Changing content type at root level

            val index = indexOf(target)

            set(index, target.copy(contentType = targetType))

            // treat toggle block children move.

            if (target.contentType == ContentType.Toggle) {

                // move toggle block children to root level

                if (target.children.isNotEmpty()) {

                    val children = target.children.map { it.copy(parentId = "") }

                    target.children.clear()

                    // add children to root revel

                    if (index < size - 1) {
                        val slice = slice(index + 1 until size)
                        removeAll { slice.contains(it) }
                        addAll(children + slice)
                    } else {
                        addAll(children)
                    }
                }
            }
        }

    } ?: throw IllegalStateException("Could not find target by id: $targetId")
}

/**
 * Deletes block by id.
 * @param targetId id of the block to delete
 */
fun Document.delete(targetId : String) {
    val block = search(targetId)

    check(block != null) { "Could not found block with id : $targetId" }

    if (block.parentId.isNotEmpty()) {
        val parent = search(block.parentId)
        check(parent != null) { "Could not found parent by id: ${block.parentId}" }
        parent.children.removeIf { it.id == block.id }
    } else {
        removeIf { it.id == block.id }
    }

}

/**
 * @param targetId id of the block to update.
 * @param targetContentUpdate new content for the block that is being updated.
 */
fun Document.updateContent(targetId : String, targetContentUpdate : Content) {
    val current = search(targetId)
    if (current != null) {
        if (current.parentId.isNotEmpty()) {
            val parent = search(current.parentId)
            if (parent != null) {
                val index = parent.children.indexOf(current)
                parent.children[index] = current.copy(content = targetContentUpdate)
            }
        } else {
            val index = indexOfFirst { block -> block.id == targetId }
            set(index, current.copy(content = targetContentUpdate))
        }
    }
}

/**
 * Fixes number order inside document.
 */
fun Document.fixNumberOrder() {
    var number = 0

    forEach { block ->
        if (block.isNumberedList()) {
            number++
            block.setNumber(number)
        } else {
            number = 0
        }
        if (block.children.isNotEmpty()) {
            block.children.fixNumberOrder()
        }
    }
}

/**
 * @param indent current indent (starting with 0)
 * @return a document representation adapted for rendering.
 *
 */
fun Document.toView(indent : Int = 0) : List<BlockView> {

    val mapper = BlockViewMapper()

    val result = mutableListOf<BlockView>()

    forEach { block ->

        result.add(mapper.mapToView(model = block, indent = indent))

        if (block.isToggle()) {
            if (block.children.isNotEmpty() && block.state.expanded) {
                result.addAll(block.children.toView(indent.inc()))
            }
        } else if (block.isList()) {
            if (block.children.isNotEmpty()) {
                result.addAll(block.children.toView(indent.inc()))
            }
        } else {
            if (block.children.isNotEmpty()) {
                result.addAll(block.children.toView(indent))
            }
        }
    }

    return result
}

@Throws(IllegalStateException::class)
fun Document.consume(consumerId : String, consumableId : String) {
    search(consumerId)?.let { consumer ->
        if (consumer.isConsumer())
            search(consumableId)?.let { consumable ->
                val parent = search(consumable.parentId)
                if (parent != null) {
                    val index = parent.children.indexOf(consumable)
                    parent.children.removeAt(index)
                    consumer.children.add(consumable.copy(parentId = consumer.id))
                } else {
                    val index = indexOf(consumable)
                    removeAt(index)
                    consumer.children.add(consumable.copy(parentId = consumer.id))
                }
            } ?: throw IllegalStateException("Could not find consumable with id: $consumableId")

    } ?: throw IllegalStateException("Could not find consumer with id: $consumerId")

}

fun Document.moveAfter(previousId : String, targetId : String) {

    search(previousId)?.let { previous ->

        search(targetId)?.let { target ->

            if (previous.hasParent()) {

                search(previous.parentId)?.let { parent ->

                    if (target.hasParent() && target.parentId != parent.id) {
                        search(target.parentId)?.let { targetParent ->
                            targetParent.children.removeIf { child -> child.id == targetId }
                        } ?: throw IllegalStateException("Could not found parent for target id: $targetId")
                    } else {
                        removeIf { block -> block.id == target.id }
                    }

                    val result = mutableListOf<Block>()

                    parent.children.forEach { child ->
                        if (child.id != targetId)
                            result.add(child)
                        if (child.id == previousId)
                            result.add(target.copy(parentId = parent.id))
                    }

                    parent.children.apply {
                        clear()
                        addAll(result)
                    }

                } ?: throw IllegalStateException("Could not find parent for previous block by parent id: ${previous.parentId}")

            } else {

                if (target.hasParent()) {

                    if (target.parentId != previous.parentId) {
                        search(target.parentId)?.let { targetParent ->
                            targetParent.children.removeIf { child -> child.id == targetId }
                        } ?: throw IllegalStateException("Could not found parent for target id: $targetId")
                    }

                    val result = mutableListOf<Block>()

                    forEach { child ->
                        result.add(child)
                        if (child.id == previousId)
                            result.add(target.copy(parentId = previous.parentId))
                    }

                    clear()
                    addAll(result)

                } else {

                    val result = mutableListOf<Block>()

                    forEach { child ->
                        if (child.id != targetId)
                            result.add(child)
                        if (child.id == previousId)
                            result.add(target.copy(parentId = previous.parentId))
                    }

                    clear()
                    addAll(result)
                }
            }

        } ?: throw IllegalStateException("Could not find target block with id: $targetId")

    } ?: throw IllegalStateException("Could not find previous block with id: $previousId")
}

/**
 * Inserts a new block after given block according to block hierarchy.
 * @param previousBlockId id of the previous block (new block is inserted after this block)
 */
fun Document.insertNewBlockAfter(previousBlockId : String) {

    search(previousBlockId)?.let { previous ->

        val newContentType = if (previous.isCheckbox() || previous.isList()) previous.contentType else ContentType.P

        val newBlock = Block.new(
            parentId = previous.parentId,
            contentType = newContentType
        )

        if (previous.parentId.isNotEmpty()) {

            search(previous.parentId)?.let { parent ->

                val result = mutableListOf<Block>()

                parent.children.forEach { block ->
                    result.add(block)
                    if (block.id == previousBlockId) result.add(newBlock)
                }

                parent.children.apply {
                    clear()
                    addAll(result)
                }

            } ?: throw IllegalStateException("Could not found parent for previous item with parent id: ${previous.parentId}")

        } else {

            val result = mutableListOf<Block>()

            forEach { block ->
                result.add(block)
                if (block.id == previousBlockId) result.add(newBlock)
            }

            this.clear()
            this.addAll(result)
        }


    } ?: throw IllegalStateException("Could not found previous block with id: $previousBlockId")
}

/**
 * Applies given action on every block of this document.
 * Only a variable field of a block can be modified by this method.
 * @param action action on block instance.
 */
fun Document.applyToAll(action : (Block) -> Unit) {
    forEach { block ->
        action(block)
        block.children.applyToAll(action)
    }
}
