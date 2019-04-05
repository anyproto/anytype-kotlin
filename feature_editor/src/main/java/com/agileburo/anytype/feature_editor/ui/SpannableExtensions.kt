package com.agileburo.anytype.feature_editor.ui

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.BulletSpan
import android.text.style.ClickableSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import com.agileburo.anytype.feature_editor.domain.Mark
import java.lang.IllegalArgumentException

fun SpannableString.addMarks(marks: List<Mark>, textView: TextView, click: (String) -> Unit) =
    apply {
        marks.forEach {
            when (it.type) {
                Mark.MarkType.BOLD -> setSpan(
                    StyleSpan(Typeface.BOLD),
                    it.start.toInt(),
                    it.end.toInt(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                Mark.MarkType.ITALIC -> setSpan(
                    StyleSpan(Typeface.ITALIC),
                    it.start.toInt(),
                    it.end.toInt(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                Mark.MarkType.STRIKE_THROUGH -> setSpan(
                    StrikethroughSpan(),
                    it.start.toInt(),
                    it.end.toInt(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                Mark.MarkType.CODE -> setSpan(
                    CodeBlockSpan(Typeface.DEFAULT),
                    it.start.toInt(),
                    it.end.toInt(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                Mark.MarkType.HYPERTEXT -> {
                    textView.movementMethod = LinkMovementMethod.getInstance()
                    withClickableSpan(it.param, it.start.toInt(), it.end.toInt(), click)
                }
                else -> throw IllegalArgumentException("Not supported type of marks : ${it.type}")
            }
        }
    }

fun SpannableString.withClickableSpan(
    param: String,
    start: Int,
    end: Int,
    onClickListener: (String) -> Unit
): SpannableString {
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View?) = onClickListener(param)
    }
    require(start <= end) { "Marks start should be <= end!" }
    setSpan(
        clickableSpan,
        start,
        end,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return this
}

fun SpannableString.withBulletSpan(gapWidth: Int, start: Int): SpannableString {
    setSpan(BulletSpan(gapWidth), start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return this
}

