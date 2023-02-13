package com.anytypeio.anytype.domain.templates

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use-case for getting templates for specific [ObjectType]
 */
class GetTemplates(
    private val repo: BlockRepository,
    private val dispatchers: AppCoroutineDispatchers
) : ResultInteractor<GetTemplates.Params, List<ObjectWrapper.Basic>>(dispatchers.io) {

    override suspend fun doWork(params: Params): List<ObjectWrapper.Basic> {
        return withContext(dispatchers.io) {
            try {
                repo.searchObjects(
                    filters = listOf(
                        DVFilter(
                            relation = Relations.IS_ARCHIVED,
                            condition = DVFilterCondition.EQUAL,
                            value = false
                        ),
                        DVFilter(
                            relation = Relations.IS_DELETED,
                            condition = DVFilterCondition.EQUAL,
                            value = false
                        ),
                        DVFilter(
                            relation = Relations.TYPE,
                            condition = DVFilterCondition.EQUAL,
                            value = ObjectTypeIds.TEMPLATE
                        ),
                        DVFilter(
                            relation = Relations.TARGET_OBJECT_TYPE,
                            condition = DVFilterCondition.EQUAL,
                            value = params.type
                        )
                    ),
                    keys = listOf(Relations.ID, Relations.NAME),
                    sorts = emptyList(),
                    fulltext = "",
                    offset = 0,
                    limit = 0
                ).map { obj ->
                    ObjectWrapper.Basic(obj)
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * @property [type] id of the object type, whose templates we are interested in.
     */
    data class Params(val type: Id)
}