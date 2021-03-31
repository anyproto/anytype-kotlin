package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.MainConfig
import com.anytypeio.anytype.domain.page.CloseBlock.Params

/**
 * Use-case for closing a smart block by id.
 * @see Params
 */
open class CloseBlock(
    private val repo: BlockRepository
) : BaseUseCase<Unit, Params>() {

    override suspend fun run(params: Params) = try {
        repo.closePage(params.id).let {
            Either.Right(it)
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    /**
     * @property id page's id
     */
    data class Params(val id: String) {
        companion object {
            fun reference() = Params(id = MainConfig.REFERENCE_PAGE_ID)
        }
    }
}