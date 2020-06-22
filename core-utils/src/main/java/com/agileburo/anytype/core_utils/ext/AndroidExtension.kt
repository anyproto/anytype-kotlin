package com.agileburo.anytype.core_utils.ext

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Rect
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.InputType
import android.text.Spanned
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

fun Context.dimen(res: Int): Float {
    return resources
        .getDimension(res)
}

fun Context.imm(): InputMethodManager {
    return getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
}

fun RecyclerView.ViewHolder.imm(): InputMethodManager {
    return itemView.context.imm()
}

fun Fragment.dimen(@DimenRes res: Int): Int = resources.getDimension(res).toInt()

fun View.height(spec: Int = View.MeasureSpec.UNSPECIFIED): Int {
    return measure(spec, spec).let { measuredHeight }
}

fun RecyclerView.ViewHolder.dimen(@DimenRes res: Int): Int {
    return itemView.context.resources.getDimension(res).toInt()
}

fun RecyclerView.ViewHolder.res(@StringRes res: Int): String {
    return itemView.context.resources.getString(res)
}

@Deprecated("Fix getColumnIndex() issue!")
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
const val MIME_VIDEO_ALL = "video/*"
const val MIME_IMAGE_ALL = "image/*"
const val MIME_FILE_ALL = "*/*"

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

fun Context.dp(value: Float) =
    TypedValue.applyDimension(COMPLEX_UNIT_DIP, value, resources.displayMetrics)

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
    getSpans(0, length, T::class.java).forEach { removeSpan(it) }
}

fun getVideoFileIntent(mediaType: String): Intent {
    val intent =
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        } else {
            Intent(Intent.ACTION_PICK, MediaStore.Video.Media.INTERNAL_CONTENT_URI)
        }
    return intent.apply {
        type = mediaType
        action = Intent.ACTION_GET_CONTENT
        putExtra("return-data", true)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}

fun String.getFileName(mime: String?): String =
    if (mime != null) {
        "$this.${mime.substringAfter("/")}"
    } else {
        this
    }

fun Int.addDot(): String = "$this."

fun EditText.multilineIme(action: Int, inputType: Int) {
    imeOptions = action
    this.inputType = inputType
    setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
    setHorizontallyScrolling(false)
    maxLines = Integer.MAX_VALUE
}

fun TextView.getCursorOffsetY(): Int? =
    (parent as? ViewGroup)?.let { parentView ->
        val start = selectionStart
        with(layout) {
            val line = getLineForOffset(start)
            val baseLine = getLineBaseline(line)
            val ascent = getLineAscent(line)
            val y = baseLine + ascent
            return y + parentView.top
        }
    }

fun View.indentize(indent: Int, defIndent: Int, margin: Int) =
    updateLayoutParams<RecyclerView.LayoutParams> {
        apply {
            val extra = indent * defIndent
            leftMargin = margin + extra
        }
    }

fun Fragment.clipboard() : ClipboardManager {
    return requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
}