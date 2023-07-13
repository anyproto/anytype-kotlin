package com.anytypeio.anytype.ui_settings.account.repo

import android.content.Context
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import java.io.File
import java.io.FileOutputStream

@Deprecated("To be deleted")
class DebugSpaceFileContentSaver(
    private val context: Context,
    dispatchers: AppCoroutineDispatchers,
) : ResultInteractor<DebugSpaceFileContentSaver.Params, File>(dispatchers.io) {

    data class Params(
        val content: String,
        val filename: String,
        val folderName: String = DEFAULT_FOLDER_NAME
    )

    override suspend fun doWork(params: Params): File {
        val cacheDir = context.cacheDir

        require(cacheDir != null) { "Impossible to cache files!" }

        // Creating folder

        val downloadFolder = File("${cacheDir.path}/${params.folderName}/").apply {
            mkdirs()
        }

        val resultFilePath = "${cacheDir.path}/${params.folderName}/${params.filename}"
        val resultFile = File(resultFilePath)

        // Writing content

        val tempFileFolderPath = "${downloadFolder.absolutePath}/${TEMP_FOLDER_NAME}"
        val tempDir = File(tempFileFolderPath)
        if (tempDir.exists()) tempDir.deleteRecursively()
        tempDir.mkdirs()

        val tempResult = File(tempFileFolderPath, params.filename)

        FileOutputStream(tempResult).use { stream ->
            stream.write(params.content.toByteArray())
        }

        tempResult.renameTo(resultFile)

        // Clearing

        tempDir.deleteRecursively()

        // Sending file

        return resultFile
    }

    companion object {
        const val DEFAULT_FOLDER_NAME = "debug_space"
        const val TEMP_FOLDER_NAME = "tmp"
    }
}