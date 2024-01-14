package com.anytypeio.anytype.core_models.ext

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Block.Content
import com.anytypeio.anytype.core_models.Document
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ThemeColor

/**
 * Maps blocks to its children using id as a key
 */
fun List<Block>.asMap(): Map<Id, List<Block>> {
    val map: MutableMap<Id, List<Block>> = mutableMapOf()
    forEach { block ->
        map[block.id] = block.children.mapNotNull { child -> find { it.id == child } }
    }
    return map
}

fun List<Block>.graph(): Map<Id, List<Id>> {
    val map: MutableMap<Id, List<Id>> = mutableMapOf()
    forEach { block ->
        map[block.id] = block.children.mapNotNull { child -> find { it.id == child }?.id }
    }
    return map
}

/**
 * Returns all descendants' (children, grand-children, etc) ids for this [parent]
 */
fun Map<Id, List<Block>>.descendants(parent: Id): List<Id> {
    val children = getValue(parent)
    val ids = children.map { it.id }
    val result = mutableListOf<Id>().apply { addAll(ids) }
    ids.forEach { id -> result.addAll(descendants(id)) }
    return result
}

/**
 * @return ids of only parent blocks from given selection, excluding all children (nested blocks)
 */
fun List<Block>.parents(selection: Iterable<Id>): List<Id> {
    val excluded = mutableListOf<Id>()
    forEach { block ->
        if (selection.contains(block.id)) {
            block.children.forEach { child ->
                if (selection.contains(child)) excluded.add(child)
            }
        }
    }
    return selection - excluded
}

/**
 * Finds title block for a [Document]
 * @return title block or null if there's no title present
 */
fun Document.title(): Block? {
    val header = firstOrNull { block ->
        val cnt = block.content
        cnt is Content.Layout && cnt.type == Content.Layout.Type.HEADER
    } ?: return null
    val children = filter { header.children.contains(it.id) }
    return children.firstOrNull { child ->
        val cnt = child.content
        cnt is Content.Text && cnt.style == Content.Text.Style.TITLE
    }
}

fun Document.titleId(): Id? {
    return find { block ->
        block.content is Content.Text && block.content.isTitle()
    }?.id
}

/**
 * Transform block structure for rendering purposes.
 * @param anchor a root or a parent block for some children blocks.
 */
fun Map<String, List<Block>>.asRender(anchor: String): List<Block> {
    val children = getValue(anchor)
    val result = mutableListOf<Block>()
    children.forEach { child ->
        when (child.content) {
            is Content.Text,
            is Content.Link,
            is Content.Divider,
            is Content.Bookmark,
            is Content.File -> {
                result.add(child)
                result.addAll(asRender(child.id))
            }
            is Content.Layout -> {
                result.addAll(asRender(child.id))
            }
            else -> {}
        }
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

fun Block.getFirstLinkOrObjectMarkupParam(range: IntRange?): String? {
    if (range == null) return null
    val marks = this.content.asText().marks
    return marks.filter { mark ->
        mark.type == Content.Text.Mark.Type.LINK || mark.type == Content.Text.Mark.Type.OBJECT
    }
        .firstOrNull { mark: Content.Text.Mark ->
            mark.rangeIntersection(range) > 0
        }.let { mark: Content.Text.Mark? -> mark?.param }
}

fun Block.getSubstring(range: IntRange): String = content.asText().text.substring(range)

fun Block.textColor(): ThemeColor {
    if (content is Content.Text)
        return content.parseThemeTextColor()
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

/**
 * Adds Text.Mark.Mention with params to block and updates block text with mention title and space
 *
 * @param mentionText page title
 * @param mentionId page id to add as Markup param
 * @param from add [mentionText] from this position in text
 * @param mentionTrigger char @ + some entered text, will be removed from text
 * @return block with updated marks and text
 */
fun Block.addMention(
    mentionText: String,
    mentionId: String,
    from: Int,
    mentionTrigger: String
): Block {

    val content = this.content.asText()
    val marks = content.marks

    //We are adding space behind mention
    val newText = "$mentionText "

    val updateMarks = marks.shift(
        from = from,
        length = newText.length - mentionTrigger.length
    )
        .addMark(
            mark = Content.Text.Mark(
                type = Content.Text.Mark.Type.MENTION,
                range = IntRange(
                    start = from,
                    endInclusive = from + mentionText.length
                ),
                param = mentionId
            )
        )

    return this.copy(
        content = Content.Text(
            marks = updateMarks,
            style = this.textStyle(),
            text = try {
                content.text.replaceRangeWithWord(
                    replace = newText,
                    from = from,
                    to = from + mentionTrigger.length
                )
            } catch (e: Exception) {
                content.text
            },
            color = content.color,
            isChecked = content.isChecked,
            align = content.align,
            iconEmoji = null,
            iconImage = null
        )
    )
}

fun Block.supportNesting(): Boolean {
    val supported = listOf(
        Content.Text.Style.P,
        Content.Text.Style.CHECKBOX,
        Content.Text.Style.NUMBERED,
        Content.Text.Style.TOGGLE,
        Content.Text.Style.CHECKBOX,
        Content.Text.Style.BULLET
    )
    return when (content) {
        is Content.Text -> supported.contains(content.style)
        is Content.Smart -> true
        is Content.Link -> true
        else -> false
    }
}

/**
 * Insert [replace] word in String starting from index [from],
 * also removed all chars in range [from]..[to]
 */
fun String.replaceRangeWithWord(replace: String, from: Int, to: Int): String {
    check(from in 0..this.length && to in 0..this.length) { "Unexpected parameters: [${from}, ${to}], length: $length" }
    val start = this.substring(0, from)
    val end = this.substring(to, this.length)
    return "$start$replace$end"
}

inline fun <reified T> Block.content(): T {
    return content as T
}

fun Document.updateTextContent(
    target: String,
    text: String,
    marks: List<Content.Text.Mark>
): Document = this.map { block ->
    if (block.id == target) {
        block.copy(
            content = block.content<Content.Text>().copy(
                text = text,
                marks = marks
            )
        )
    } else {
        block
    }
}

fun List<Block>.getChildrenIdsList(parent: Id): List<String> {
    val root = this.firstOrNull { it.id == parent }
    return root?.children ?: emptyList()
}

fun List<Block>.isAllTextAndNoneCodeBlocks(): Boolean =
    all { block ->
        block.content is Content.Text && block.content.style != Content.Text.Style.CODE_SNIPPET
    }

fun List<Block>.isAllTextBlocks(): Boolean =
    all { block ->
        block.content is Content.Text
    }

fun Block.Content.Text.parseThemeTextColor() : ThemeColor {
    return color?.let { ThemeColor.fromCode(it) } ?: ThemeColor.DEFAULT
}