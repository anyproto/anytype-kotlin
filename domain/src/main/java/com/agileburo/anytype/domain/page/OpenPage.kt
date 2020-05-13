package com.agileburo.anytype.domain.page

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.config.MainConfig
import com.agileburo.anytype.domain.event.model.Payload

open class OpenPage(
    private val repo: BlockRepository
) : BaseUseCase<Payload, OpenPage.Params>() {

    override suspend fun run(params: Params) = try {
        repo.openPage(params.id).let {
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