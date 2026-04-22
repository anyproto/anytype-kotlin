package com.anytypeio.anytype.domain.download

import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

/**
 * Toggles the middleware auto-download feature on or off and its wifi-only gate.
 *
 * Wraps the middleware `FileSetAutoDownload` RPC. When [Params.enabled] is false,
 * middleware stops auto-downloading files regardless of the size limit. When
 * [Params.wifiOnly] is true, middleware only downloads on non-metered networks.
 *
 * Caller responsibility: if the feature is being enabled, pair this call with
 * [FileAutoDownloadSetLimit] to push the current size threshold to middleware.
 */
class FileSetAutoDownload @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers,
) : ResultInteractor<FileSetAutoDownload.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        repo.fileSetAutoDownload(
            enabled = params.enabled,
            wifiOnly = params.wifiOnly
        )
    }

    data class Params(
        val enabled: Boolean,
        val wifiOnly: Boolean
    )
}
