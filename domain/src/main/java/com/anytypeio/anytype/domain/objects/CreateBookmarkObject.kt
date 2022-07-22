package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for creating bookmark object from url.
 */
class CreateBookmarkObject(
    private val repo: BlockRepository
) : BaseUseCase<Id, Url>() {
    override suspend fun run(params: Url) = safe {
        repo.createBookmarkObject(url = params)
    }
}