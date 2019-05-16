package com.agileburo.anytype.feature_editor.presentation.mapper

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.view.View
import com.agileburo.anytype.feature_editor.domain.Block
import com.agileburo.anytype.feature_editor.domain.Content
import com.agileburo.anytype.feature_editor.domain.Mark
import com.agileburo.anytype.feature_editor.presentation.model.BlockView
import com.agileburo.anytype.feature_editor.ui.CodeBlockSpan
import timber.log.Timber
import java.lang.IllegalArgumentException

interface ViewMapper<in D, out V> {
    fun mapToView(model: D): V
}

class BlockViewMapper : ViewMapper<Block, BlockView> {

    override fun mapToView(model: Block): BlockView {
        return BlockView(
            id = model.id,
            contentType = model.contentType,
            content = mapTextContent(model.content)
        )
    }

    private fun mapTextContent(content: Content.Text): BlockView.Content.Text {
        return BlockView.Content.Text(
            text = fromMarksToSpannable(text = content.text, marks = content.marks),
            param = BlockView.ContentParam(
                mapOf(
                    "number" to content.param.number,
                    "checked" to content.param.checked
                )
            )
        )
    }
}

private fun fromMarksToSpannable(text: String, marks: List<Mark>): SpannableStringBuilder {
    return SpannableStringBuilder(text).apply {
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
                    val clickableSpan = object : ClickableSpan() {
                        override fun onClick(widget: View?) {
                            //onClickListener.invoke()
                            Timber.d("On link clicked !!!")
                        }
                    }
                    setSpan(clickableSpan, it.start, it.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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

}

fun SpannableString.withClickableSpan(clickablePart: String, onClickListener: () -> Unit): SpannableString {
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View?) = onClickListener.invoke()
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