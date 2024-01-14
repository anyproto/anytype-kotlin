package com.anytypeio.anytype.presentation.util.downloader

import android.content.Context
import android.net.Uri
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import java.io.File

/**
 * Base class for downloading files in the local storage for later sharing
 * using middleware for data receiving
 * */
abstract class MiddlewareShareDownloader(
    private val context: Context,
    private val uriFileProvider: UriFileProvider,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<MiddlewareShareDownloader.Params, MiddlewareShareDownloader.Response>(dispatchers.io) {

    data class Params(
        val objectId: Id,
        val name: String
    )

    data class Response(
        val uri: Uri,
        val path: String
    )

    /**
     * @param objectId id of Object File
     * @param path is local storage path to the file created
     * @return path to the file in the local storage
     * */
    abstract suspend fun downloadFile(objectId: Id, path: String): String

    override suspend fun doWork(params: Params): Response {
        val cacheDir = context.cacheDir

        require(cacheDir != null) { "Impossible to cache files!" }

        val downloadFolder = File("${cacheDir.path}/${params.objectId}").apply { mkdirs() }

        val resultFilePath = "${cacheDir.path}/${params.objectId}/${params.name}"
        val resultFile = File(resultFilePath)

        if (!resultFile.exists()) {
            val tempFileFolderPath = "${downloadFolder.absolutePath}/tmp"
            val tempDir = File(tempFileFolderPath)
            if (tempDir.exists()) tempDir.deleteRecursively()
            tempDir.mkdirs()

            val tempResult = File(
                downloadFile(
                    objectId = params.objectId,
                    path = tempFileFolderPath
                )
            )

            tempResult.renameTo(resultFile)
        }
        return Response(
            uri = uriFileProvider.getUriForFile(resultFile),
            path = resultFilePath
        )
    }
}

