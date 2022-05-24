package com.anytypeio.anytype.core_utils.ext

import android.Manifest
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.InputType
import android.text.Spannable
import android.text.Spanned
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_utils.const.FileConstants.REQUEST_FILE_SAF_CODE
import com.anytypeio.anytype.core_utils.const.FileConstants.REQUEST_MEDIA_CODE
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

fun Uri.parseImagePath(context: Context): String {
    context.contentResolver.query(
        this,
        arrayOf(MediaStore.Images.Media.DATA),
        null,
        null,
        null
    )?.use { cursor ->
        cursor.moveToFirst()
        val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        return cursor.getString(idx)
    } ?: this.path ?: throw IllegalStateException("Cold not get real path")
    throw IllegalStateException("Cold not get real path")
}

fun Throwable.timber() = Timber.e("Get error : ${this.message}")

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

inline fun <reified T> Spannable.removeSpans() {
    getSpans(0, length, T::class.java).forEach { removeSpan(it) }
}

fun String.getFileName(mime: String?): String =
    if (mime != null) {
        "$this.${mime.substringAfter("/")}"
    } else {
        this
    }

fun Int.addDot(): String = "$this."

fun EditText.multilineIme(action: Int) {
    imeOptions = action
    inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
    setRawInputType(
        InputType.TYPE_CLASS_TEXT
                or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                or InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
    )
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

fun View.indentize(indent: Int, defIndent: Int, margin: Int) {
    updateLayoutParams<RecyclerView.LayoutParams> {
        apply {
            val extra = indent * defIndent
            leftMargin = margin + extra
        }
    }
}

fun Fragment.clipboard(): ClipboardManager {
    return requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
}

fun Fragment.screen(): Point {
    val wm = (requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager)
    val display = wm.defaultDisplay
    val p = Point()
    display.getSize(p)
    return p
}

fun Activity.screen(): Point {
    val wm = (getSystemService(Context.WINDOW_SERVICE) as WindowManager)
    val display = wm.defaultDisplay
    val p = Point()
    display.getSize(p)
    return p
}

fun Drawable.setDrawableColor(color: Int) {
    this.colorFilter = BlendModeColorFilterCompat
        .createBlendModeColorFilterCompat(color, BlendModeCompat.SRC_ATOP)
}

fun View.focusAndShowKeyboard() {
    /**
     * This is to be called when the window already has focus.
     */
    fun View.showTheKeyboardNow() {
        if (isFocused) {
            post {
                // We still post the call, just in case we are being notified of the windows focus
                // but InputMethodManager didn't get properly setup yet.
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    requestFocus()
    if (hasWindowFocus()) {
        // No need to wait for the window to get focus.
        showTheKeyboardNow()
    } else {
        // We need to wait until the window gets focus.
        viewTreeObserver.addOnWindowFocusChangeListener(
            object : ViewTreeObserver.OnWindowFocusChangeListener {
                override fun onWindowFocusChanged(hasFocus: Boolean) {
                    // This notification will arrive just before the InputMethodManager gets set up.
                    if (hasFocus) {
                        this@focusAndShowKeyboard.showTheKeyboardNow()
                        // It’s very important to remove this listener once we are done.
                        viewTreeObserver.removeOnWindowFocusChangeListener(this)
                    }
                }
            })
    }
}

fun String.normalizeUrl(): String =
    if (!startsWith("http://") && !startsWith("https://")) "https://$this" else this

fun Context.isPermissionGranted(mimeType: Mimetype): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && mimeType == Mimetype.MIME_FILE_ALL) {
        true
    } else {
        val readExternalStorage: Int = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        readExternalStorage == PackageManager.PERMISSION_GRANTED
    }
}

fun Activity.shouldShowRequestPermissionRationaleCompat(permission: String) =
    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

/**
 * [requestCode] is used only for picking media.
 * Should be refactored here:
 * https://app.clickup.com/t/2cbqneb
 */
fun Fragment.startFilePicker(mime: Mimetype, requestCode: Int? = null) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
            type = mime.value
        }
        val code = if (mime == Mimetype.MIME_FILE_ALL) {
            REQUEST_FILE_SAF_CODE
        } else {
            REQUEST_MEDIA_CODE
        }
        startActivityForResult(intent, requestCode ?: code)
    } else {
        val intent =
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            } else {
                Intent(Intent.ACTION_PICK, MediaStore.Video.Media.INTERNAL_CONTENT_URI)
            }
        intent.apply {
            type = mime.value
            action = Intent.ACTION_GET_CONTENT
            putExtra("return-data", true)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivityForResult(intent, requestCode ?: REQUEST_MEDIA_CODE)
    }
}

/**
 * Use this class for picking images from external storage
 */
class GetImageContract : ActivityResultContract<Int, Uri?>() {
    override fun createIntent(context: Context, input: Int?): Intent {
        return Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (resultCode == Activity.RESULT_OK) {
            return intent?.data
        }
        return null
    }
}