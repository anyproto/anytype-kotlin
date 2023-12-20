package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

/**
 * Use-case for creating bookmark object from url.
 */
class CreateBookmarkObject @Inject constructor(
    private val repo: BlockRepository
) : BaseUseCase<Id, CreateBookmarkObject.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.createBookmarkObject(
            space = params.space,
            url = params.url,
            details = params.details
        )
    }

    data class Params(
        val space: Id,
        val url: Url,
        val details: Struct = emptyMap()
    )
}