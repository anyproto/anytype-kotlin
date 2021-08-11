package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.icon.DocumentEmojiIconProvider

/**
 * A use-case for creating a new page.
 * Currently used for creating a new page inside a dashboard.
 */
class CreatePage(
    private val repo: BlockRepository,
    private val documentEmojiIconProvider: DocumentEmojiIconProvider
) : BaseUseCase<Id, CreatePage.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.createPage(
            ctx = params.ctx,
            emoji = documentEmojiIconProvider.random()
        )
    }

    /**
     * @property [ctx] context (parent) for this new page.
     */
    data class Params(val ctx: Id?)
}