package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.ResultatInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SearchObjectsByIdWithSubscription(
    private val repo: BlockRepository
) : ResultatInteractor<SearchObjectsByIdWithSubscription.Params, SearchResult>() {

    override suspend fun execute(params: Params): SearchResult {
        return repo.searchObjectsByIdWithSubscription(
            space = params.space,
            subscription = params.subscription,
            ids = params.ids,
            keys = params.keys
        )
    }

    class Params(
        val space: SpaceId,
        val subscription: Id,
        val ids: List<Id>,
        val keys: List<String>
    )
}