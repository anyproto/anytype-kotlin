package com.anytypeio.anytype.presentation.util.downloader

import android.content.Context
import android.net.Uri
import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.domain.base.ResultInteractor
import java.io.File

/**
 * Base class for downloading files in the local storage for later sharing
 * using middleware for data receiving
 * */
abstract class MiddlewareShareDownloader(
    private val context: Context,
    private val uriFileProvider: UriFileProvider
) : ResultInteractor<MiddlewareShareDownloader.Params, Uri>() {

    data class Params(
        val hash: Hash,
        val name: String
    )

    /**
     * @param hash is a some middleware id
     * @param path is local storage path to the file created
     * @return path to the file in the local storage
     * */
    abstract suspend fun downloadFile(hash: String, path: String): String

    override suspend fun doWork(params: Params): Uri {
        val cacheDir = context.cacheDir

        require(cacheDir != null) { "Impossible to cache files!" }

        val downloadFolder = File("${cacheDir.path}/${params.hash}").apply { mkdirs() }

        val resultFilePath = "${cacheDir.path}/${params.hash}/${params.name}"
        val resultFile = File(resultFilePath)

        if (!resultFile.exists()) {
            val tempFileFolderPath = "${downloadFolder.absolutePath}/tmp"
            val tempDir = File(tempFileFolderPath)
            if (tempDir.exists()) tempDir.deleteRecursively()
            tempDir.mkdirs()

            val tempResult = File(downloadFile(params.hash, tempFileFolderPath))

            tempResult.renameTo(resultFile)
        }
        return uriFileProvider.getUriForFile(resultFile)
    }
}

