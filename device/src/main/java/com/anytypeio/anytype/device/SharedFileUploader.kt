package com.anytypeio.anytype.device

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.anytypeio.anytype.domain.device.FileSharer
import com.anytypeio.anytype.localization.R
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import timber.log.Timber

class SharedFileUploader @Inject constructor(
    private val context: Context
) : FileSharer {

    override fun getPath(uri: String): String {
        if (BuildConfig.DEBUG) Timber.d("Getting path for: ${uri}")
        val parsed = Uri.parse(uri)
        checkNotNull(parsed)
        return parsePathFromUri(parsed)
    }

    private fun parsePathFromUri(extra: Uri) : String {
        val name = if (extra.scheme == CONTENT_URI_SCHEME) {
            context.contentResolver.query(
                extra,
                null,
                null,
                null,
                null
            ).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx != -1) {
                        cursor.getString(idx)
                    } else {
                        context.resources.getString(R.string.untitled)
                    }
                } else {
                    context.resources.getString(R.string.untitled)
                }
            }
        } else {
            extra.path!!.substring(extra.path!!.lastIndexOf("/"))
        }
        val inputStream = context.contentResolver.openInputStream(extra)
        val cacheDir = context.getExternalFilesDir(null)
        if (cacheDir != null && !cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        var path = ""
        inputStream?.use { input ->
            val newFile = File(cacheDir?.path + "/" + name);
            FileOutputStream(newFile).use { output ->
                val buffer = ByteArray(1024)
                var read: Int = input.read(buffer)
                while (read != -1) {
                    output.write(buffer, 0, read)
                    read = input.read(buffer)
                }
            }
            path = newFile.path
        }

        return path
    }

    companion object {
        const val CONTENT_URI_SCHEME = "content"
    }

}