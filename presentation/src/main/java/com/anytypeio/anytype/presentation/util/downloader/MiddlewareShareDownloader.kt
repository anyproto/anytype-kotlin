package com.anytypeio.anytype.presentation.util.downloader

import android.content.Context
import android.net.Uri
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.presentation.util.TEMPORARY_DIRECTORY_NAME
import kotlinx.coroutines.withContext
import java.io.File

class MiddlewareShareDownloader(
    private val repo: BlockRepository,
    private val dispatchers: AppCoroutineDispatchers,
    private val context: Context,
    private val uriFileProvider: UriFileProvider
) : ResultInteractor<MiddlewareShareDownloader.Params, Uri>() {

    data class Params(
        val hash: Hash,
        val name: String
    )

    override suspend fun doWork(params: Params) = withContext(dispatchers.io) {
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

            val tempResult = File(
                repo.downloadFile(
                    Command.DownloadFile(
                        hash = params.hash,
                        path = tempFileFolderPath
                    )
                )
            )

            tempResult.renameTo(resultFile)
        }
        uriFileProvider.getUriForFile(resultFile)
    }
}