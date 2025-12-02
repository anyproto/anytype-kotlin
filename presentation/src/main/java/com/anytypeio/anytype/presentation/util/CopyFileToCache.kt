package com.anytypeio.anytype.presentation.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.anytypeio.anytype.core_utils.ext.msg
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Interface defining the contract for copying files to a cache directory.
 */
interface CopyFileToCacheDirectory {

    /**
     * Executes the file copying operation for the given [uri].
     *
     * @param uri The URI of the file to be copied.
     * @param scope The [CoroutineScope] used for the asynchronous operation.
     * @param listener The [CopyFileToCacheStatus] listener to handle events during the operation.
     */
    fun execute(uri: Uri, scope: CoroutineScope, listener: CopyFileToCacheStatus)

    suspend fun copy(uri: String): String?

    /**
     * Cancels the ongoing file copying operation.
     */
    fun cancel()

    /**
     * Checks if the file copying operation is currently active.
     *
     * @return `true` if the operation is active, `false` otherwise.
     */
    fun isActive(): Boolean

    fun delete(uri: String): Boolean
}

/**
 * Listener interface to handle events during the file copying operation.
 */
interface CopyFileToCacheStatus {
    fun onCopyFileStart()
    fun onCopyFileResult(result: String?, fileName: String? = null)
    fun onCopyFileError(msg: String)
}

const val SCHEME_CONTENT = "content"
const val CHAR_SLASH = '/'
const val TEMPORARY_DIRECTORY_NAME = "TemporaryFiles"

/**
 * Represents the status of a file copying operation.
 */
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

    override fun execute(uri: Uri, scope: CoroutineScope, listener: CopyFileToCacheStatus) {
        getNewPathInCacheDir(
            uri = uri,
            scope = scope,
            listener = listener,
        )
    }

    override suspend fun copy(uri: String): String? {
        return copyFileToCacheDir(uri)
    }

    override fun cancel() {
        job?.cancel()
        mContext?.get()?.deleteTemporaryFolder()
    }

    private fun getNewPathInCacheDir(
        uri: Uri,
        scope: CoroutineScope,
        listener: CopyFileToCacheStatus
    ) {
        job = scope.launch {
            // Notify listener on the caller's thread (typically main thread)
            listener.onCopyFileStart()
            var path: String? = null
            try {
                withContext(Dispatchers.IO) {
                    path = copyFileToCacheDir(uri)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error while getNewPathInCacheDir")
                if (isActive) {
                    listener.onCopyFileError(e.msg())
                }
            } finally {
                if (isActive) {
                    listener.onCopyFileResult(path)
                }
            }
        }
    }

    private fun copyFileToCacheDir(uri: Uri): String? {
        var newFile: File? = null
        mContext?.get()?.let { context: Context ->
            val cacheDir = context.getExternalFilesDirTemp()
            if (cacheDir != null && !cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            try {
                // Pre-calculate filename BEFORE opening input stream
                // This ensures no FD leak if getFileName throws
                val fileName = getFileName(context, uri)
                newFile = File(cacheDir?.path + "/" + fileName)
                Timber.d("Start copy file to cache : ${newFile?.path}")

                // Open input stream and immediately protect with .use {}
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(newFile).use { output ->
                        input.copyTo(output)
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

    private fun copyFileToCacheDir(
        uri: String
    ): String? {
        var newFile: File? = null
        mContext?.get()?.let { context: Context ->
            val cacheDir = context.getExternalFilesDirTemp()
            if (cacheDir != null && !cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            try {
                // Parse URI once and pre-calculate filename BEFORE opening input stream
                val parsedUri = Uri.parse(uri)
                val fileName = getFileName(context, parsedUri)
                newFile = File(cacheDir?.path + "/" + fileName)
                Timber.d("Start copy file to cache : ${newFile?.path}")

                // Open input stream and immediately protect with .use {}
                context.contentResolver.openInputStream(parsedUri)?.use { input ->
                    FileOutputStream(newFile).use { output ->
                        input.copyTo(output)
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

    override fun delete(uri: String): Boolean {
        val context = mContext?.get() ?: return false
        return try {
            val path = Uri.parse(uri).path ?: return false
            val file = File(path)

            // Optional: check if file is in cache or external files dir
            val allowedRoots = listOfNotNull(
                context.cacheDir?.absolutePath,
                context.getExternalFilesDir(null)?.absolutePath
            )

            if (allowedRoots.any { file.absolutePath.startsWith(it) }) {
                if (!file.exists()) {
                    Timber.w("File does not exist: $path")
                    return false
                }

                val deleted = file.delete()
                Timber.d("Attempting to delete file: $path → deleted=$deleted")
                deleted
            } else {
                Timber.w("Blocked delete attempt outside allowed folders: $path")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting file at $uri")
            false
        }
    }
}

/**
 * Network mode-specific implementation of the [CopyFileToCacheDirectory] interface.
 *
 * @param context The application context.
 */
class NetworkModeCopyFileToCacheDirectory(context: Context) : CopyFileToCacheDirectory {

    private var mContext: WeakReference<Context>? = null
    private var job: Job? = null

    init {
        mContext = WeakReference(context)
    }

    override fun isActive(): Boolean = job?.isActive == true

    override fun execute(uri: Uri, scope: CoroutineScope, listener: CopyFileToCacheStatus) {
        getNewPathInCacheDir(
            uri = uri,
            scope = scope,
            listener = listener,
        )
    }

    override suspend fun copy(uri: String): String? {
        throw UnsupportedOperationException()
    }

    override fun cancel() {
        job?.cancel()
    }

    private fun getNewPathInCacheDir(
        uri: Uri,
        scope: CoroutineScope,
        listener: CopyFileToCacheStatus
    ) {
        job = scope.launch {
            // Notify listener on the caller's thread (typically main thread)
            listener.onCopyFileStart()
            var path: String? = null
            var fileName: String? = null
            try {
                withContext(Dispatchers.IO) {
                    val pair = copyFileToCacheDir(uri)
                    path = pair.first
                    fileName = pair.second
                }
            } catch (e: Exception) {
                Timber.e(e, "Error while getNewPathInCacheDir")
                if (isActive) {
                    listener.onCopyFileError(e.msg())
                }
            } finally {
                if (isActive) {
                    listener.onCopyFileResult(path, fileName)
                }
            }
        }
    }

    private fun copyFileToCacheDir(uri: Uri): Pair<String?, String?> {
        var newFile: File? = null
        mContext?.get()?.let { context: Context ->
            // Pre-calculate filename BEFORE opening input stream
            val fileName = getFileName(context, uri)

            val cacheDir = context.getExternalCustomNetworkDirTemp()
            if (cacheDir != null && !cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            try {
                // Prepare file path BEFORE opening input stream
                newFile = File(cacheDir?.path + "/" + CONFIG_FILE_NAME)
                Timber.d("Start copy file to cache : ${newFile?.path}")

                // Open input stream and immediately protect with .use {}
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(newFile).use { output ->
                        input.copyTo(output)
                    }
                    return Pair(newFile?.path, fileName)
                }
            } catch (e: Exception) {
                val deleteResult = newFile?.deleteRecursively()
                Timber.d("Get exception while copying file, deleteRecursively success: $deleteResult")
                Timber.e(e, "Error while coping file")
            }
        }
        return Pair(null, null)
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
        return result
    }

    override fun delete(uri: String): Boolean {
        val context = mContext?.get() ?: return false
        return try {
            val file = File(Uri.parse(uri).path ?: return false)
            val deleted = file.delete()
            Timber.d("Attempting to delete file by uri: $uri → $deleted")
            deleted
        } catch (e: Exception) {
            Timber.e(e, "Error deleting file by uri: $uri")
            false
        }
    }

    companion object {
        const val CONFIG_FILE_NAME = "configCustom.txt"
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
fun Context.getExternalFilesDirTemp(): File? = getExternalFilesDir(TEMPORARY_DIRECTORY_NAME)

/**
 * Return /storage/emulated/0/Android/data/io.anytype.app/files/networkModeConfig directory
 */
private fun Context.getExternalCustomNetworkDirTemp(): File? = getExternalFilesDir("networkModeConfig")