package com.agileburo.anytype.feature_editor.presentation.mapper

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import com.agileburo.anytype.feature_editor.domain.*
import com.agileburo.anytype.feature_editor.presentation.model.BlockView
import com.agileburo.anytype.feature_editor.presentation.model.BlockView.HeaderView.*

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
        //todo Разобраться с parentId

        return when (view) {
            is BlockView.ParagraphView -> {
                Block(
                    id = view.id,
                    parentId = "",
                    contentType = ContentType.P,
                    content = Content.Text(
                        text = view.text.toString(),
                        marks = fromSpannableToMarks(view.text),
                        param = ContentParam.empty()
                    ),
                    blockType = BlockType.Editable
                )
            }
            is BlockView.QuoteView -> {
                Block(
                    id = view.id,
                    parentId = "",
                    contentType = ContentType.Quote,
                    content = Content.Text(
                        text = view.text.toString(),
                        marks = fromSpannableToMarks(view.text),
                        param = ContentParam.empty()
                    ),
                    blockType = BlockType.Editable
                )
            }
            is BlockView.CodeSnippetView -> {
                Block(
                    id = view.id,
                    parentId = "",
                    contentType = ContentType.Code,
                    content = Content.Text(
                        text = view.text.toString(),
                        marks = fromSpannableToMarks(view.text),
                        param = ContentParam.empty()
                    ),
                    blockType = BlockType.Editable
                )
            }
            is BlockView.CheckboxView -> {
                Block(
                    id = view.id,
                    parentId = "",
                    contentType = ContentType.Check,
                    content = Content.Text(
                        text = view.text.toString(),
                        marks = fromSpannableToMarks(view.text),
                        param = ContentParam.checkbox(view.isChecked)
                    ),
                    blockType = BlockType.Editable
                )
            }
            is BlockView.HeaderView -> {

                val contentType = when(view.type) {
                    HeaderType.ONE -> ContentType.H1
                    HeaderType.TWO -> ContentType.H2
                    HeaderType.THREE -> ContentType.H3
                    HeaderType.FOUR -> ContentType.H4
                }

                Block(
                    id = view.id,
                    parentId = "",
                    contentType = contentType,
                    content = Content.Text(
                        text = view.text.toString(),
                        marks = fromSpannableToMarks(view.text),
                        param = ContentParam.empty()
                    ),
                    blockType = BlockType.Editable
                )
            }

            is BlockView.BulletView -> {
                Block(
                    id = view.id,
                    parentId = "",
                    contentType = ContentType.UL,
                    content = Content.Text(
                        text = view.text.toString(),
                        marks = fromSpannableToMarks(view.text),
                        param = ContentParam.empty()
                    ),
                    blockType = BlockType.Editable
                )
            }
            is BlockView.NumberListItemView -> {
                Block(
                    id = view.id,
                    parentId = "",
                    contentType = ContentType.NumberedList,
                    content = Content.Text(
                        text = view.text.toString(),
                        marks = fromSpannableToMarks(view.text),
                        param = ContentParam.numberedList(view.number)
                    ),
                    blockType = BlockType.Editable
                )
            }

            else -> TODO()

        }


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
        text.getSpans(0, text.length, URLSpan::class.java).forEach {
            val start = text.getSpanStart(it)
            val end = text.getSpanEnd(it)
            if (start <= end) {
                marks.add(Mark(start = start, end = end, param = it.url, type = Mark.MarkType.HYPERTEXT))
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

