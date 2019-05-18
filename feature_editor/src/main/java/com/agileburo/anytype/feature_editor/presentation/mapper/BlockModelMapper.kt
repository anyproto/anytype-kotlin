package com.agileburo.anytype.feature_editor.presentation.mapper

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import com.agileburo.anytype.feature_editor.domain.*
import com.agileburo.anytype.feature_editor.presentation.model.BlockView

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-05-15.
 */

interface ModelMapper<in V, out D> {
    fun mapToModel(view: V): D
}

class BlockModelMapper : ModelMapper<BlockView, Block> {

    override fun mapToModel(view: BlockView): Block {
        //todo Разобраться с parentId и param
        return Block(
            id = view.id, parentId = "",
            content = Content.Text(
                text = view.content.text.toString(),
                param = ContentParam.empty(),
                marks = fromSpannableToMarks(view.content.text)
            ),
            contentType = view.contentType
        )
    }

    private fun fromSpannableToMarks(content: CharSequence): List<Mark> {
        val text = SpannableString(content)
        val marks = ArrayList<Mark>()
        text.getSpans(0, text.length, StyleSpan::class.java).forEach {
            val start = text.getSpanStart(it)
            val end = text.getSpanEnd(it)
            if (start <= end) {
                marks.add(Mark(start = start, end = end, param = "", type = getStyleMarkType(it.style)))
            }
        }
        text.getSpans(0, text.length, StrikethroughSpan::class.java).forEach {
            val start = text.getSpanStart(it)
            val end = text.getSpanEnd(it)
            if (start <= end) {
                marks.add(Mark(start = start, end = end, param = "", type = Mark.MarkType.STRIKE_THROUGH))
            }
        }
        return marks
    }

    private fun getStyleMarkType(style: Int) =
        when (style) {
            Typeface.BOLD -> Mark.MarkType.BOLD
            Typeface.ITALIC -> Mark.MarkType.ITALIC
            else -> Mark.MarkType.UNDEFINED
        }
}

