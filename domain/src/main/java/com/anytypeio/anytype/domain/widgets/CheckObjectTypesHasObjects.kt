package com.anytypeio.anytype.domain.widgets

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.search.SearchObjects
import javax.inject.Inject

/**
 * Checks which object types have at least one object instance.
 * Used for filtering the Objects section to show only types with existing objects.
 * 
 * This performs efficient existence checks (limit=1) rather than fetching all objects,
 * making it suitable for the grouped types widget which displays types as navigation only.
 */
class CheckObjectTypesHasObjects @Inject constructor(
    private val searchObjects: SearchObjects,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CheckObjectTypesHasObjects.Params, Map<Id, Boolean>>(dispatchers.io) {

    override suspend fun doWork(params: Params): Map<Id, Boolean> {
        val result = mutableMapOf<Id, Boolean>()
        
        params.typeIds.forEach { typeId ->
            // Search for objects of this type with limit=1 to check existence
            val filters = buildList {
                add(
                    DVFilter(
                        relation = Relations.TYPE,
                        condition = DVFilterCondition.EQUAL,
                        value = typeId
                    )
                )
                add(
                    DVFilter(
                        relation = Relations.IS_ARCHIVED,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    )
                )
                add(
                    DVFilter(
                        relation = Relations.IS_DELETED,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    )
                )
                // Exclude templates
                add(
                    DVFilter(
                        relation = Relations.TYPE,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = ObjectTypeIds.TEMPLATE
                    )
                )
            }
            
            val searchResult = searchObjects.run(
                SearchObjects.Params(
                    space = params.space,
                    filters = filters,
                    limit = 1, // Only need to check if at least one exists
                    keys = emptyList() // Don't need any relation data
                )
            )
            
            result[typeId] = searchResult.getOrNull()?.isNotEmpty() == true
        }
        
        return result
    }

    data class Params(
        val space: SpaceId,
        val typeIds: List<Id>
    )
}
