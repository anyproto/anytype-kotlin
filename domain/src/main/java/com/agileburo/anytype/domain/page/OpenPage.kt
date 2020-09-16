package com.agileburo.anytype.domain.page

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Result
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.event.model.Payload

open class OpenPage(
    private val repo: BlockRepository
) : BaseUseCase<Result<Payload>, OpenPage.Params>() {

    override suspend fun run(params: Params) = safe { repo.openPage(params.id) }

    /**
     * @property id page's id
     */
    data class Params(val id: String)
}