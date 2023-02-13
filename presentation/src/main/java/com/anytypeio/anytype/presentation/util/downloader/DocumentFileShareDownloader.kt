package com.anytypeio.anytype.presentation.util.downloader

import android.content.Context
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class DocumentFileShareDownloader(
    private val repo: BlockRepository,
    context: Context,
    uriFileProvider: UriFileProvider,
    dispatchers: AppCoroutineDispatchers
) : MiddlewareShareDownloader(context, uriFileProvider, dispatchers) {

    override suspend fun downloadFile(hash: String, path: String) = repo.downloadFile(
        Command.DownloadFile(hash = hash, path = path)
    )
}