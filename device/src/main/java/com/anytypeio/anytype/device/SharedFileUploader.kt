package com.anytypeio.anytype.device

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.provider.OpenableColumns
import com.anytypeio.anytype.domain.device.FileSharer
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class SharedFileUploader @Inject constructor(
    private val context: Context
) : FileSharer {

    override fun getPath(uri: String): String? {
        val parsed = Uri.parse(uri)
        checkNotNull(parsed)
        return parsePathFromFile(parsed)
    }

    private fun parsePathFromFile(extra: Uri) : String {
        val name = if (extra.scheme == "content") {
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
                        "Untitled"
                    }
                } else {
                    "Untitled"
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

}