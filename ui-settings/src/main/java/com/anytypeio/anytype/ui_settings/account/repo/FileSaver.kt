package com.anytypeio.anytype.ui_settings.account.repo

import android.content.Context
import android.net.Uri
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.presentation.util.downloader.UriFileProvider
import java.io.File
import java.io.FileOutputStream

class FileSaver(
    private val context: Context,
    private val uriFileProvider: UriFileProvider
) : ResultInteractor<FileSaver.Params, Uri>() {

    data class Params(
        val content: String,
        val name: String
    )

    override suspend fun doWork(params: Params): Uri {
        val cacheDir = context.cacheDir

        require(cacheDir != null) { "Impossible to cache files!" }

        val downloadFolder = File("${cacheDir.path}/debug_sync/").apply { mkdirs() }

        val resultFilePath = "${cacheDir.path}/${params.name}"
        val resultFile = File(resultFilePath)

        if (!resultFile.exists()) {
            val tempFileFolderPath = "${downloadFolder.absolutePath}/tmp"
            val tempDir = File(tempFileFolderPath)
            if (tempDir.exists()) tempDir.deleteRecursively()
            tempDir.mkdirs()

            val tempResult = File(tempFileFolderPath, params.name)
            FileOutputStream(tempResult).use {
                it.write(params.content.toByteArray())
            }

            tempResult.renameTo(resultFile)
        }
        return uriFileProvider.getUriForFile(resultFile)
    }
}