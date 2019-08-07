package com.agileburo.anytype.feature_editor.presentation.mapper

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.view.View
import com.agileburo.anytype.feature_editor.domain.*
import com.agileburo.anytype.feature_editor.presentation.model.BlockView
import com.agileburo.anytype.feature_editor.ui.CodeBlockSpan

interface ViewMapper<in D, out V> {
    fun mapToView(model: D, indent : Int = 0): V
}

class BlockViewMapper : ViewMapper<Block, BlockView> {

    override fun mapToView(model: Block, indent : Int): BlockView {
        return when (model.blockType) {
            is BlockType.Editable -> {
                when (model.contentType) {
                    is ContentType.P -> {
                        BlockView.ParagraphView(
                            indent = indent,
                            id = model.id,
                            text = fromMarksToSpannable(
                                marks = (model.content as Content.Text).marks,
                                text = model.content.text
                            ),
                            focused = model.state.focused
                        )
                    }
                    is ContentType.H1 -> {
                        BlockView.HeaderView(
                            id = model.id,
                            text = fromMarksToSpannable(
                                marks = (model.content as Content.Text).marks,
                                text = model.content.text
                            ),
                            type = BlockView.HeaderView.HeaderType.ONE,
                            indent = indent,
                            focused = model.state.focused
                        )
                    }
                    is ContentType.H2 -> {
                        BlockView.HeaderView(
                            id = model.id,
                            text = fromMarksToSpannable(
                                marks = (model.content as Content.Text).marks,
                                text = model.content.text
                            ),
                            type = BlockView.HeaderView.HeaderType.TWO,
                            indent = indent,
                            focused = model.state.focused
                        )
                    }
                    is ContentType.H3 -> {
                        BlockView.HeaderView(
                            id = model.id,
                            text = fromMarksToSpannable(
                                marks = (model.content as Content.Text).marks,
                                text = model.content.text
                            ),
                            type = BlockView.HeaderView.HeaderType.THREE,
                            indent = indent,
                            focused = model.state.focused
                        )
                    }
                    is ContentType.H4 -> {
                        BlockView.HeaderView(
                            id = model.id,
                            text = fromMarksToSpannable(
                                marks = (model.content as Content.Text).marks,
                                text = model.content.text
                            ),
                            type = BlockView.HeaderView.HeaderType.FOUR,
                            indent = indent,
                            focused = model.state.focused
                        )
                    }
                    is ContentType.Quote -> {
                        BlockView.QuoteView(
                            id = model.id,
                            text = fromMarksToSpannable(
                                marks = (model.content as Content.Text).marks,
                                text = model.content.text
                            ),
                            indent = indent,
                            focused = model.state.focused
                        )
                    }
                    is ContentType.Code -> {
                        BlockView.CodeSnippetView(
                            id = model.id,
                            text = fromMarksToSpannable(
                                marks = (model.content as Content.Text).marks,
                                text = model.content.text
                            ),
                            indent = indent,
                            focused = model.state.focused
                        )
                    }
                    is ContentType.Check -> {
                        BlockView.CheckboxView(
                            id = model.id,
                            text = fromMarksToSpannable(
                                marks = (model.content as Content.Text).marks,
                                text = model.content.text
                            ),
                            isChecked = model.content.param.checked,
                            indent = indent,
                            focused = model.state.focused
                        )
                    }
                    is ContentType.NumberedList -> {
                        BlockView.NumberListItemView(
                            id = model.id,
                            text = fromMarksToSpannable(
                                marks = (model.content as Content.Text).marks,
                                text = model.content.text
                            ),
                            number = model.content.param.number,
                            indent = indent,
                            focused = model.state.focused
                        )
                    }
                    is ContentType.UL -> {
                        BlockView.BulletView(
                            id = model.id,
                            text = fromMarksToSpannable(
                                marks = (model.content as Content.Text).marks,
                                text = model.content.text
                            ),
                            indent = indent,
                            focused = model.state.focused
                        )
                    }
                    is ContentType.Toggle -> {
                        BlockView.ToggleView(
                            id = model.id,
                            text = fromMarksToSpannable(
                                marks = (model.content as Content.Text).marks,
                                text = model.content.text
                            ),
                            indent = indent,
                            expanded = model.state.expanded,
                            focused = model.state.focused
                        )
                    }
                    else -> {
                        throw NotImplementedError("${model.contentType} is not supported")
                    }
                }
            }
            is BlockType.Page -> {
                BlockView.LinkToPageView(
                    id = model.id,
                    title = (model.content as Content.Page).id
                )
            }
            is BlockType.BookMark -> {
                BlockView.BookmarkView(
                    id = model.id,
                    title = (model.content as Content.Bookmark).title,
                    description = model.content.description,
                    url = model.content.url,
                    image = model.content.images.first().url,
                    indent = indent
                )
            }
            is BlockType.Divider -> {
                BlockView.DividerView(
                    id = model.id,
                    indent = indent
                )
            }
            is BlockType.Image -> {
                BlockView.PictureView(
                    id = model.id,
                    url = (model.content as Content.Picture).url,
                    indent = indent
                )
            }
            else -> TODO()
        }
    }

    private fun fromMarksToSpannable(text: String, marks: List<Mark>) =
        SpannableString(text).apply {
            marks.forEach {
                when (it.type) {
                    Mark.MarkType.BOLD -> setSpan(
                        StyleSpan(Typeface.BOLD),
                        it.start,
                        it.end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    Mark.MarkType.ITALIC -> setSpan(
                        StyleSpan(Typeface.ITALIC),
                        it.start,
                        it.end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    Mark.MarkType.STRIKE_THROUGH -> setSpan(
                        StrikethroughSpan(),
                        it.start,
                        it.end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    Mark.MarkType.CODE -> setSpan(
                        CodeBlockSpan(Typeface.DEFAULT),
                        it.start,
                        it.end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    Mark.MarkType.HYPERTEXT -> {
                        setSpan(URLSpan(it.param), it.start, it.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

//TODO решить задачу EditText + Clickable UrlSpan
//                    val clickableSpan = object : ClickableSpan() {
//                        override fun onClick(widget: View?) {
//                            //onClickListener.invoke()
//                            Timber.d("On link clicked !!!")
//                        }
//                    }
//                    setSpan(clickableSpan, it.start, it.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//                    val method = BetterLinkMovementMethod.getInstance()
//                    textView.movementMethod = method
//                    textView.setOnTouchListener { _, event ->
//                        method.onTouchEvent(textView, textView.text as Spannable, event)
//                                || itemView.onTouchEvent(event)
//                    }
//                        withClickableSpan(it.param, it.start.toInt(), it.end.toInt(), click)
                    }
                    else -> throw IllegalArgumentException("Not supported type of marks : ${it.type}")
                }
            }
        }

    fun SpannableString.withClickableSpan(clickablePart: String, onClickListener: () -> Unit): SpannableString {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) = onClickListener.invoke()
        }
        val clickablePartStart = indexOf(clickablePart)
        setSpan(
            clickableSpan,
            clickablePartStart,
            clickablePartStart + clickablePart.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return this
    }
}