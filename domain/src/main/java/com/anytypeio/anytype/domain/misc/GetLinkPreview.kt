package com.anytypeio.anytype.domain.misc

import com.anytypeio.anytype.core_models.LinkPreview
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class GetLinkPreview @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Url, LinkPreview>(dispatchers.io) {
    override suspend fun doWork(params: Url): LinkPreview {
        return repo.getLinkPreview(url = params)
    }
}