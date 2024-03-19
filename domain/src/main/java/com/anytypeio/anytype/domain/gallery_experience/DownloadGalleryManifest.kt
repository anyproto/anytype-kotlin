package com.anytypeio.anytype.domain.gallery_experience

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.ManifestInfo
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class DownloadGalleryManifest @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository
) : ResultInteractor<DownloadGalleryManifest.Params, ManifestInfo?>(dispatchers.io) {

    override suspend fun doWork(params: Params): ManifestInfo? {
        val command = Command.DownloadGalleryManifest(
            url = params.url
        )
        return repo.downloadGalleryManifest(command)
    }

    class Params(val url: String)
}