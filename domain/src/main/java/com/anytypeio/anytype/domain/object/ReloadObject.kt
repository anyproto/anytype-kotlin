package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for reloading object content.
 * @see [Params]
 */
class ReloadObject(
    private val repo: BlockRepository
) : BaseUseCase<Unit, ReloadObject.Params>() {

    override suspend fun run(params: Params) = safe {
        when(params) {
            is Params.FromUrl -> {
                repo.fetchBookmarkObject(
                    ctx = params.ctx,
                    url = params.url
                )
            }
        }
    }

    sealed class Params {
        abstract val ctx: Id
        data class FromUrl(
            override val ctx: Id,
            val url: Url
        ) : Params()
    }
}