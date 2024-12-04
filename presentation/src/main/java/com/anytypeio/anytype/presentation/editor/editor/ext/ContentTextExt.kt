package com.anytypeio.anytype.presentation.editor.editor.ext

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.MAX_SNIPPET_SIZE
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.replaceRangeWithWord
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.Markup.Companion.NON_EXISTENT_OBJECT_MENTION_NAME
import com.anytypeio.anytype.presentation.extension.shift
import timber.log.Timber

fun Block.Content.Text.getTextAndMarks(
    details: Block.Details,
    marks: List<Markup.Mark>,
    fieldParser: FieldParser
): Pair<String, List<Markup.Mark>> {
    if (details.details.isEmpty() ||
        marks.none { it is Markup.Mark.Mention }
    ) {
        return Pair(text, marks)
    }
    var updatedText = text
    val updatedMarks = marks.toMutableList()
    updatedMarks.sortBy { it.from }
    try {
        updatedMarks.forEach { mark ->
            if (mark !is Markup.Mark.Mention || mark.param.isBlank()) return@forEach
            var newName = if (mark is Markup.Mark.Mention.Deleted) {
                NON_EXISTENT_OBJECT_MENTION_NAME
            } else {
                details.details.getProperObjectName(id = mark.param) ?: return@forEach
            }
            val oldName = updatedText.substring(mark.from, mark.to)
            if (newName != oldName) {
                if (newName.isEmpty()) newName = Relations.RELATION_NAME_EMPTY
                val d = newName.length - oldName.length
                updatedText = updatedText.replaceRangeWithWord(
                    replace = newName,
                    from = mark.from,
                    to = mark.to
                )
                updatedMarks.shift(
                    start = mark.from,
                    length = d
                )
            }
        }
    } catch (e: Exception) {
        Timber.e(e, "Error while update mention markups")
        return Pair(text, marks)
    }
    return Pair(updatedText, updatedMarks)
}

private fun Map<Id, Block.Fields>.getProperObjectName(id: Id?): String? {
    if (id == null) return null
    val layoutCode = this[id]?.layout?.toInt()
    return if (layoutCode == ObjectType.Layout.NOTE.code) {
        this[id]?.snippet?.replace("\n", " ")?.take(MAX_SNIPPET_SIZE)
    } else {
        this[id]?.name
    }
}

