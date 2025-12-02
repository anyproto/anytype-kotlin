package com.anytypeio.anytype.presentation.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.anytypeio.anytype.core_utils.ext.msg
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

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

    /**
     * Copies the file at the given [uri] to the cache directory.
     *
     * @param uri The URI string of the file to be copied.
     * @return The path of the copied file in the cache directory.
     * @throws IllegalStateException if the cache directory is not available or the input stream cannot be opened.
     * @throws IllegalArgumentException if the file name cannot be determined.
     */
    suspend fun copy(uri: String): String

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

    /**
     * Deletes a file at the given path that was previously copied to the cache.
     * @param path The absolute file path (not a content:// URI)
     * @return true if deletion was successful, false otherwise
     */
    fun delete(path: String): Boolean
}

/**
 * Listener interface to handle events during the file copying operation.
 */
interface CopyFileToCacheStatus {
    fun onCopyFileStart()
    fun onCopyFileResult(result: String?, fileName: String? = null)
    fun onCopyFileError(msg: String)
}

private const val SCHEME_CONTENT = "content"
private const val CHAR_SLASH = '/'
const val TEMPORARY_DIRECTORY_NAME = "TemporaryFiles"
private const val NETWORK_MODE_DIRECTORY = "networkModeConfig"
private const val NETWORK_MODE_CONFIG_FILE = "configCustom.txt"

/**
 * Configuration for file copying behavior.
 */
sealed class CopyFileConfig {
    /**
     * Keeps the original filename from the URI.
     * Used for general file attachments (editor, chats, etc.)
     */
    data class KeepOriginalName(
        val directory: String,
        val deleteOnCancel: Boolean = true
    ) : CopyFileConfig()

    /**
     * Uses a fixed filename regardless of the source URI.
     * Used for network config import where the file must be at a specific location.
     */
    data class FixedFileName(
        val directory: String,
        val fileName: String,
        val deleteOnCancel: Boolean = false
    ) : CopyFileConfig()
}

/**
 * Factory function to create a CopyFileToCacheDirectory for general file attachments.
 * Files are copied to TemporaryFiles directory with their original names.
 */
fun defaultCopyFileToCacheDirectory(context: Context): CopyFileToCacheDirectory =
    CopyFileToCacheDirectoryImpl(
        context = context,
        config = CopyFileConfig.KeepOriginalName(
            directory = TEMPORARY_DIRECTORY_NAME,
            deleteOnCancel = true
        )
    )

/**
 * Factory function to create a CopyFileToCacheDirectory for network config import.
 * Files are copied to networkModeConfig directory with a fixed name (configCustom.txt).
 */
fun networkModeCopyFileToCacheDirectory(context: Context): CopyFileToCacheDirectory =
    CopyFileToCacheDirectoryImpl(
        context = context,
        config = CopyFileConfig.FixedFileName(
            directory = NETWORK_MODE_DIRECTORY,
            fileName = NETWORK_MODE_CONFIG_FILE,
            deleteOnCancel = false
        )
    )

/**
 * Unified implementation of [CopyFileToCacheDirectory] that supports different
 * file copying configurations via [CopyFileConfig].
 */
class CopyFileToCacheDirectoryImpl(
    private val context: Context,
    private val config: CopyFileConfig
) : CopyFileToCacheDirectory {

    private var job: Job? = null
    private var lastCreatedFilePath: String? = null

    override fun isActive(): Boolean = job?.isActive == true

    override fun execute(uri: Uri, scope: CoroutineScope, listener: CopyFileToCacheStatus) {
        // Cancel previous job and clean up any partial file from it
        job?.cancel()
        cleanupTrackedFile()

        job = scope.launch {
            listener.onCopyFileStart()
            try {
                val (path, originalFileName) = withContext(Dispatchers.IO) {
                    copyFileToCacheDir(uri)
                }
                if (isActive) {
                    lastCreatedFilePath = null // Clear on success, file ownership transferred
                    listener.onCopyFileResult(path, originalFileName)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Error while copying file to cache")
                if (isActive) {
                    listener.onCopyFileError(e.msg())
                }
            }
        }
    }

    override suspend fun copy(uri: String): String = withContext(Dispatchers.IO) {
        val result = copyFileToCacheDir(Uri.parse(uri)).first
        lastCreatedFilePath = null  // Clear tracking - ownership transferred to caller
        result
    }

    override fun cancel() {
        job?.cancel()
        cleanupTrackedFile()
    }

    /**
     * Cleans up any tracked temporary file from a cancelled or replaced operation.
     * Only deletes if config allows (KeepOriginalName with deleteOnCancel = true).
     */
    private fun cleanupTrackedFile() {
        if (config is CopyFileConfig.KeepOriginalName && config.deleteOnCancel) {
            lastCreatedFilePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    val deleted = file.delete()
                    Timber.d("Cleanup: deleted temp file $path: $deleted")
                }
                lastCreatedFilePath = null
            }
        }
    }

    /**
     * Copies file to cache directory.
     * @return Pair of (path to copied file, original filename from URI)
     * @throws IllegalStateException if the cache directory is not available or the input stream cannot be opened.
     * @throws IllegalArgumentException if the file name cannot be determined (for KeepOriginalName config).
     */
    @Throws(IllegalStateException::class, IllegalArgumentException::class)
    private fun copyFileToCacheDir(uri: Uri): Pair<String, String?> {
        val originalFileName = getFileName(uri)

        val cacheDir = context.getExternalFilesDir(getDirectoryName())
            ?: throw IllegalStateException("External files directory is not available")

        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        val targetFileName = getTargetFileName(originalFileName, uri)
        val newFile = File(cacheDir.path + "/" + targetFileName)
        lastCreatedFilePath = newFile.path
        Timber.d("Start copy file to cache: ${newFile.path}")

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("Could not open input stream for uri: $uri")

            inputStream.use { input ->
                FileOutputStream(newFile).use { output ->
                    input.copyTo(output)
                }
            }
            return Pair(newFile.path, originalFileName)
        } catch (e: Exception) {
            val deleteResult = newFile.deleteRecursively()
            Timber.d("Exception while copying file, deleteRecursively success: $deleteResult")
            throw e
        }
    }

    private fun getDirectoryName(): String = when (config) {
        is CopyFileConfig.KeepOriginalName -> config.directory
        is CopyFileConfig.FixedFileName -> config.directory
    }

    private fun getTargetFileName(originalFileName: String?, uri: Uri): String {
        return when (config) {
            is CopyFileConfig.KeepOriginalName -> {
                if (originalFileName.isNullOrEmpty()) {
                    throw IllegalArgumentException("Could not determine file name for uri: $uri")
                }
                originalFileName
            }
            is CopyFileConfig.FixedFileName -> config.fileName
        }
    }

    private fun getFileName(uri: Uri): String? {
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
                    result = path.substring(cut + 1)
                }
            }
        }
        return result
    }

    override fun delete(path: String): Boolean {
        return try {
            // Validate this is a file path, not a content:// or file:// URI
            if (path.startsWith("content://") || path.startsWith("file://")) {
                Timber.w("delete() expects a file path, not a URI: $path")
                return false
            }

            val file = File(path)

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
            Timber.e(e, "Error deleting file at $path")
            false
        }
    }
}
