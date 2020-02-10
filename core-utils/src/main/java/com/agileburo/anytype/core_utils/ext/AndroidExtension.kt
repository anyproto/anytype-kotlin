package com.agileburo.anytype.core_utils.ext

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.net.Uri
import android.provider.MediaStore
import android.text.Annotation
import android.text.Editable
import android.text.Spanned
import android.view.TouchDelegate
import android.view.View
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

fun Context.dimen(res: Int): Float {
    return resources
        .getDimension(res)
}

fun Uri.parsePath(context: Context): String {

    val result: String?

    val cursor = context.contentResolver.query(
        this,
        null,
        null,
        null, null
    )

    if (cursor == null) {
        result = this.path
    } else {
        cursor.moveToFirst()
        val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        result = cursor.getString(idx)
        cursor.close()
    }

    return result ?: throw IllegalStateException("Cold not get real path")
}

fun Throwable.timber() = Timber.e("Get error : ${this.message}")

const val DATE_FORMAT_MMMdYYYY = "MMM d, yyyy"
const val KEY_ROUNDED = "key"
const val VALUE_ROUNDED = "rounded"

fun Long.formatToDateString(pattern: String, locale: Locale): String {
    val formatter = SimpleDateFormat(pattern, locale)
    return formatter.format(Date(this))
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

private fun expandViewHitArea(parent: View, child: View) {
    parent.post {
        val parentRect = Rect()
        val childRect = Rect()
        parent.getHitRect(parentRect)
        child.getHitRect(childRect)
        childRect.left = 0
        childRect.top = 0
        childRect.right = parentRect.width()
        childRect.bottom = parentRect.height()
        parent.touchDelegate = TouchDelegate(childRect, child)
    }
}

fun <T> hasSpan(spanned: Spanned, clazz: Class<T>): Boolean {
    val limit = spanned.length
    return spanned.nextSpanTransition(0, limit, clazz) < limit
}

inline fun <reified T> Editable.removeSpans() {
    val allSpans = getSpans(0, length, T::class.java)
    for (span in allSpans) {
        removeSpan(span)
    }
}

fun Editable.removeRoundedSpans(): Editable {
    this.getSpans(0, length, Annotation::class.java).forEach { span ->
        if (span.key == KEY_ROUNDED && span.value == VALUE_ROUNDED) removeSpan(span)
    }
    return this
}