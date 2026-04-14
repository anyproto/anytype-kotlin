package com.anytypeio.anytype.domain.download

import com.anytypeio.anytype.core_models.FileDownloadLimit
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

/**
 * Pushes the user's Offline Downloads preference to the Go middleware.
 *
 * Mirrors the iOS `syncAutoDownloadToMiddleware()` flow from anytype-swift
 * PR #4628 (IOS-5804):
 *  1. Always call `FileSetAutoDownload(enabled, wifiOnly)` first.
 *  2. If enabled, then call `FileAutoDownloadSetLimit(sizeLimitMebibytes)`.
 *     When the feature is off the limit call is skipped because middleware
 *     ignores the limit when auto-download is disabled.
 *
 *  Cellular gating: `wifiOnly = !useCellular`. Middleware handles the actual
 *  network-type gating, so the client does not need to observe network state.
 */
class SyncFileDownloadLimitToMiddleware @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers,
) : ResultInteractor<SyncFileDownloadLimitToMiddleware.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        repo.fileSetAutoDownload(
            enabled = params.limit.isEnabled,
            wifiOnly = !params.useCellular
        )
        if (params.limit.isEnabled) {
            repo.fileAutoDownloadSetLimit(
                sizeLimitMebibytes = params.limit.sizeLimitMebibytes
            )
        }
    }

    data class Params(
        val limit: FileDownloadLimit,
        val useCellular: Boolean
    )
}
