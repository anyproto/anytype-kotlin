package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * A use-case for creating a new page.
 * Currently used for creating a new page inside a dashboard.
 */
class CreatePage(
    private val repo: BlockRepository
) : ResultInteractor<CreatePage.Params, Id>() {

    override suspend fun doWork(params: Params): Id = repo.createPage(
        ctx = params.ctx,
        emoji = null,
        isDraft = params.isDraft,
        type = params.type,
        template = params.template
    )

    /**
     * @property [ctx] context (parent) for this new page.
     * @property [type] type of created object
     * @property [isDraft] should this object be in Draft state
     * @property [template] id of the template for this object (optional)
     */
    data class Params(
        val ctx: Id?,
        val type: String?,
        val emoji: String?,
        val isDraft: Boolean?,
        val template: Id? = null
    ) {
        constructor(
            isDraft: Boolean?,
        ) : this(
            ctx = null,
            type = null,
            emoji = null,
            isDraft = isDraft,
            template = null)
    }
}