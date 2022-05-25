package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SearchObjectsByIdWithSubscription(
    private val repo: BlockRepository
) : BaseUseCase<SearchResult, SearchObjectsByIdWithSubscription.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.searchObjectsByIdWithSubscription(
            subscription = params.subscription,
            ids = params.ids,
            keys = params.keys
        )
    }

    class Params(
        val subscription: Id,
        val ids: List<Id>,
        val keys: List<String>
    )
}