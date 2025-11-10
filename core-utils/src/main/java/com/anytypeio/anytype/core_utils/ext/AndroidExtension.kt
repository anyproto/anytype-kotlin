package com.anytypeio.anytype.core_utils.ext

import android.app.Activity
import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_MIME_TYPES
import android.content.res.Resources
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.provider.Settings
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
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_utils.const.FileConstants.REQUEST_FILE_SAF_CODE
import com.anytypeio.anytype.core_utils.const.FileConstants.REQUEST_MEDIA_CODE
import com.anytypeio.anytype.core_utils.const.MimeTypes.MIME_EXTRA_IMAGE_VIDEO
import com.anytypeio.anytype.core_utils.const.MimeTypes.MIME_EXTRA_YAML
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.presentation.util.downloader.UriFileProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File
import timber.log.Timber

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

fun expandViewHitArea(parent: View, child: View) {
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
    setRawInputType(
        InputType.TYPE_TEXT_FLAG_MULTI_LINE
                or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                or InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
    )
    imeOptions = action
    setHorizontallyScrolling(false)
    maxLines = Integer.MAX_VALUE
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

/**
 * [requestCode] is used only for picking media.
 * Should be refactored here:
 * https://app.clickup.com/t/2cbqneb
 */
fun Fragment.startFilePicker(mime: Mimetype, requestCode: Int? = null) {
    when (mime) {
        Mimetype.MIME_IMAGE_AND_VIDEO -> startMediaPicker(mime, requestCode)
        else -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    configureTypeOfIntentForMime(this, mime)
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
                    configureTypeOfIntentForMime(this, mime)
                    action = Intent.ACTION_GET_CONTENT
                    putExtra("return-data", true)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivityForResult(intent, requestCode ?: REQUEST_MEDIA_CODE)
            }
        }
    }
}

private fun configureTypeOfIntentForMime(intent: Intent, mime: Mimetype) {
    if (mime == Mimetype.MIME_YAML) {
        intent.type = Mimetype.MIME_FILE_ALL.value
        intent.putExtra(EXTRA_MIME_TYPES, MIME_EXTRA_YAML)
    } else {
        intent.type = mime.value
    }
}

private fun Fragment.startMediaPicker(mime: Mimetype, requestCode: Int? = null) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = mime.value
        putExtra(Intent.EXTRA_MIME_TYPES, MIME_EXTRA_IMAGE_VIDEO)
    }
    startActivityForResult(intent, requestCode ?: REQUEST_MEDIA_CODE)
}

/**
 * Use this class for picking images from external storage
 */
class GetImageContract : ActivityResultContract<Int, Uri?>() {
    override fun createIntent(context: Context, input: Int): Intent {
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

fun NavController.safeNavigate(
    @IdRes currentDestinationId: Int,
    @IdRes id: Int,
    args: Bundle? = null,
    errorMessage: String? = null
) {
    val currentDest = currentDestination
    val isAtExpectedDestination = currentDest?.id == currentDestinationId
    val isInExpectedGraph = currentDest?.parent?.id == currentDestinationId
    
    if (isAtExpectedDestination || isInExpectedGraph) {
        runCatching {
            navigate(id, args)
        }.onFailure {
            Timber.e(it, "Error while navigation, $errorMessage")
        }
    } else {
        // Last resort: try to navigate anyway if we can find the destination in the graph
        runCatching {
            if (graph.findNode(id) != null) {
                navigate(id, args)
                Timber.d("Navigation succeeded despite destination mismatch: expected=$currentDestinationId, actual=${currentDest?.id}")
            } else {
                Timber.d("Skipping navigation: expected=$currentDestinationId, actual=${currentDest?.id}, parent=${currentDest?.parent?.id}, destination not found in graph")
            }
        }.onFailure {
            Timber.w(it, "Skipping navigation: expected=$currentDestinationId, actual=${currentDest?.id}, parent=${currentDest?.parent?.id}, error: $errorMessage")
        }
    }
}

fun Fragment.shareFirstFileFromPath(path: String, uriFileProvider: UriFileProvider) {
    try {
        val dirPath = File(path)
        if (dirPath.exists() && dirPath.isDirectory) {
            val files = dirPath.listFiles()
            val firstFile = files?.firstOrNull { it != null && it.exists() && it.isFile }
            if (firstFile != null) {
                val uri = uriFileProvider.getUriForFile(firstFile)
                shareFile(uri)
            } else {
                Timber.w("No valid files to share in the directory: $path")
                toast("No valid files to share in the directory.")
            }
        } else {
            Timber.w("Directory does not exist or is not a directory: $path")
            toast("Directory does not exist or is not a directory.")
        }
    } catch (e: Exception) {
        Timber.e(e, "Error while sharing file")
        toast("Could not share file: ${e.message}")
    }
}

fun Fragment.shareFileFromPath(path: String, uriFileProvider: UriFileProvider) {
    try {
        val dirPath = File(path)
        if (dirPath.exists()) {
            val uri = uriFileProvider.getUriForFile(dirPath)
            shareFile(uri)
        } else {
            toast("File does not exist.")
        }
    } catch (e: Exception) {
        Timber.e(e, "Error while sharing file")
        toast("Could not share file: ${e.message}")
    }
}

fun Fragment.shareFile(uri: Uri) {
    try {
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_STREAM, uri)
            type = requireContext().contentResolver.getType(uri)
        }
        startActivity(shareIntent)
    } catch (e: Exception) {
        if (e is ActivityNotFoundException) {
            toast("No application found to open the selected file")
        } else {
            toast("Could not open file: ${e.message}")
        }
        Timber.e(e, "Error while opening file")
    }
}

fun Intent.parseActionSendMultipleUris() : List<String> {
    val extras = getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM) ?: arrayListOf()
    return extras.mapNotNull { extra ->
        if (extra is Uri)
            extra.toString()
        else
            null
    }
}

fun Intent.parseActionSendUri() : String? {
    val extra = getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM)
    return if (extra is Uri) {
        extra.toString()
    } else {
        null
    }
}

fun bytesToHumanReadableSizeLocal(bytes: Long): String = when {
    bytes >= 1 shl 30 -> bytes.readableFileSize()
    bytes >= 1 shl 20 -> bytes.readableFileSize()
    bytes >= 1 shl 10 -> if (bytes <= 1048000L) "0 MB" else bytes.readableFileSize()
    else -> "$bytes bytes"
}

fun BaseBottomSheetComposeFragment.setupBottomSheetBehavior(paddingTop: Int) {
    (dialog as? BottomSheetDialog)?.behavior?.apply {
        isFitToContents = false
        expandedOffset = paddingTop
        state = BottomSheetBehavior.STATE_EXPANDED
        skipCollapsed = true
    }
}

fun Context.isAppInForeground(): Boolean {
    val appProcessInfo = ActivityManager.RunningAppProcessInfo()
    ActivityManager.getMyMemoryState(appProcessInfo)
    return appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
            appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
}

fun Context.openNotificationSettings() {
    val intent = try {
        // Android 8.0+ — opens your app’s notifications settings
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            // if you have channels:
            // putExtra(Settings.EXTRA_CHANNEL_ID, yourChannelId)
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    } catch (e: ActivityNotFoundException) {
        // Ultimate fallback
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    startActivity(intent)
}

/**
 * Opens the application's settings screen in the system settings.
 *
 * This extension function launches an intent that navigates the user to the
 * app-specific settings page, where permissions and other options can be managed.
 * Typically used when prompting the user to manually adjust app permissions.
 *
 * @receiver Context used to start the settings activity.
 */
fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
    startActivity(intent)
}