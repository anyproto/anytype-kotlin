package com.anytypeio.anytype.presentation.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference

interface CopyFileToCacheDirectory {

    fun execute(uri: Uri, scope: CoroutineScope, listener: OnCopyFileToCacheAction)
    fun cancel()
    fun isActive(): Boolean
}

const val SCHEME_CONTENT = "content"
const val CHAR_SLASH = '/'
const val TEMPORARY_DIRECTORY_NAME = "TemporaryFiles"

sealed class CopyFileStatus {
    data class Error(val msg: String) : CopyFileStatus()
    object Started : CopyFileStatus()
    data class Completed(val result: String?) : CopyFileStatus()
}

class DefaultCopyFileToCacheDirectory(context: Context) : CopyFileToCacheDirectory {

    private var mContext: WeakReference<Context>? = null
    private var job: Job? = null

    init {
        mContext = WeakReference(context)
    }

    override fun isActive(): Boolean = job?.isActive == true

    override fun execute(uri: Uri, scope: CoroutineScope, listener: OnCopyFileToCacheAction) {
        getNewPathInCacheDir(
            uri = uri,
            scope = scope,
            listener = listener,
        )
    }

    override fun cancel() {
        job?.cancel()
        mContext?.get()?.deleteTemporaryFolder()
    }

    private fun getNewPathInCacheDir(
        uri: Uri,
        scope: CoroutineScope,
        listener: OnCopyFileToCacheAction
    ) {
        var path: String? = null
        job = scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    path = copyFileToCacheDir(uri, listener)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error while getNewPathInCacheDir")
                listener.onCopyFileError(e.localizedMessage ?: "Unknown error")
            } finally {
                if (scope.isActive) {
                    listener.onCopyFileResult(path)
                }
            }
        }
    }

    fun copyFileToCacheDir(
        uri: Uri,
        listener: OnCopyFileToCacheAction
    ): String? {
        var newFile: File? = null
        mContext?.get()?.let { context: Context ->
            val cacheDir = context.getExternalFilesDirTemp()
            if (cacheDir != null && !cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.use { input ->
                    newFile = File(cacheDir?.path + "/" + getFileName(context, uri));
                    listener.onCopyFileStart()
                    Timber.d("Start copy file to cache : ${newFile?.path}")
                    FileOutputStream(newFile).use { output ->
                        val buffer = ByteArray(1024)
                        var read: Int = input.read(buffer)
                        while (read != -1) {
                            output.write(buffer, 0, read)
                            read = input.read(buffer)
                        }
                    }
                    return newFile?.path
                }
            } catch (e: Exception) {
                val deleteResult = newFile?.deleteRecursively()
                Timber.d("Get exception while copying file, deleteRecursively success: $deleteResult")
                Timber.e(e, "Error while coping file")
            }
        }
        return null
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == SCHEME_CONTENT) {
            context.contentResolver.query(
                uri,
                null,
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            uri.path?.let { path ->
                val cut = path.lastIndexOf(CHAR_SLASH)
                if (cut != -1) {
                    result = path.substring(cut)
                }
            }
        }
        return result
    }
}

/**
 * Delete the /storage/emulated/0/Android/data/package/files/$TEMPORARY_DIRECTORY_NAME folder.
 */
private fun Context.deleteTemporaryFolder() {
    getExternalFilesDirTemp()?.let { folder ->
        if (folder.deleteRecursively()) {
            Timber.d("${folder.absolutePath} delete successfully")
        } else {
            Timber.d("${folder.absolutePath} delete is unsuccessfully")
        }
    }
}

/**
 * Return /storage/emulated/0/Android/data/package/files/$TEMPORARY_DIRECTORY_NAME directory
 */
private fun Context.getExternalFilesDirTemp(): File? = getExternalFilesDir(TEMPORARY_DIRECTORY_NAME)

interface OnCopyFileToCacheAction {
    fun onCopyFileStart()
    fun onCopyFileResult(result: String?)
    fun onCopyFileError(msg: String)
}