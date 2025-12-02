package com.anytypeio.anytype.device

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.device.FileSharer
import com.anytypeio.anytype.localization.R
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.jvm.Throws
import kotlinx.coroutines.withContext
import timber.log.Timber

class SharedFileUploader @Inject constructor(
    private val context: Context,
    private val dispatchers: AppCoroutineDispatchers
) : FileSharer {

    override suspend fun getPath(uri: String): String = withContext(dispatchers.io) {
        if (BuildConfig.DEBUG) Timber.d("Getting path for: $uri")
        val parsed = Uri.parse(uri)
        checkNotNull(parsed)
        parsePathFromUri(parsed)
    }

    override suspend fun clear() {
        TODO("Not yet implemented")
    }

    override suspend fun getDisplayName(uri: String): String? = withContext(dispatchers.io) {
        val parsed = Uri.parse(uri)
        context.contentResolver.query(
            parsed,
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
    }

    @Throws(Exception::class)
    private fun parsePathFromUri(extra: Uri) : String {
        // Pre-calculate name BEFORE opening any file descriptors
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
            val rawPath = extra.path
            if (rawPath != null) {
                rawPath.substring(rawPath.lastIndexOf("/"))
            } else {
                ""
            }
        }

        // Setup cache directory BEFORE opening input stream
        // This ensures no FD leak if directory operations fail
        val cacheDir = context.getExternalFilesDir(null)
        if (cacheDir != null && !cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        var path = ""
        // Wrap file operations in try-catch for robust error handling
        try {
            // Open input stream and immediately protect with .use {}
            // This ensures FD is always closed, even if FileOutputStream throws
            context.contentResolver.openInputStream(extra)?.use { input ->
                val newFile = File(cacheDir?.path + "/" + name)
                FileOutputStream(newFile).use { output ->
                    input.copyTo(output)
                }
                path = newFile.path
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while copying file from URI: $extra")
        }

        return path
    }

    companion object {
        const val CONTENT_URI_SCHEME = "content"
    }

}