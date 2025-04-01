package com.anytypeio.anytype.domain.widgets

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class GetSuggestedWidgetTypes @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository,
) : ResultInteractor<GetSuggestedWidgetTypes.Params, List<ObjectWrapper.Type>>(dispatchers.io) {

    override suspend fun doWork(params: Params): List<ObjectWrapper.Type> {

        // TODO DROID-3438 open widget object preview and filter out existing object types

        val types = repo.searchObjects(
            space = params.space,
            limit = DEFAULT_LIMIT,
            filters = params.objectTypeFilters,
            keys = params.objectTypeKeys
        ).map { result ->
            ObjectWrapper.Type(result)
        }

        return types
    }

    data class Params(
        val space: Space,
        val objectTypeFilters: List<DVFilter>,
        val objectTypeKeys: List<Id>
    )
    
    companion object {
        const val DEFAULT_LIMIT = 5
    }
}