package com.anytypeio.anytype.presentation.util.downloader

import android.content.Context
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class DebugGoroutinesShareDownloader(
    private val repo: BlockRepository,
    context: Context,
    uriFileProvider: UriFileProvider,
    dispatchers: AppCoroutineDispatchers
) : MiddlewareShareDownloader(context, uriFileProvider, dispatchers) {

    override suspend fun downloadFile(hash: String, path: String): String {
        repo.debugStackGoroutines(path = path)
        return path
    }
}