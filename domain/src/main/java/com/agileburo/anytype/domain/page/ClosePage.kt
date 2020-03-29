package com.agileburo.anytype.domain.page

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.config.MainConfig
import com.agileburo.anytype.domain.page.ClosePage.Params

/**
 * Use-case for closing a page by id.
 * @see Params
 */
open class ClosePage(
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