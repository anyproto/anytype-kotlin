package com.anytypeio.anytype.presentation.templates

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.workspace.SpaceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

sealed interface ObjectTypeTemplatesContainer {

    suspend fun subscribeToTemplates(type: Id, subId: String): Flow<List<ObjectWrapper.Basic>>
    suspend fun unsubscribeFromTemplates(subId: Id)
}

class DefaultObjectTypeTemplatesContainer(
    private val storage: StorelessSubscriptionContainer,
    private val spaceManager: SpaceManager
) : ObjectTypeTemplatesContainer {

    override suspend fun subscribeToTemplates(type: Id, subId: String): Flow<List<ObjectWrapper.Basic>> {
        return if (type.isBlank()) {
            emptyFlow()
        } else {
            val params = StoreSearchParams(
                subscription = subId,
                sorts = listOf(
                    DVSort(
                        relationKey = Relations.CREATED_DATE,
                        type = Block.Content.DataView.Sort.Type.DESC
                    )
                ),
                filters = listOf(
                    DVFilter(
                        relation = Relations.IS_ARCHIVED,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    ),
                    DVFilter(
                        relation = Relations.IS_DELETED,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    ),
                    DVFilter(
                        relation = Relations.TYPE_UNIQUE_KEY,
                        condition = DVFilterCondition.EQUAL,
                        value = ObjectTypeIds.TEMPLATE
                    ),
                    DVFilter(
                        relation = Relations.TARGET_OBJECT_TYPE,
                        condition = DVFilterCondition.EQUAL,
                        value = type
                    ),
                    DVFilter(
                        relation = Relations.SPACE_ID,
                        condition = DVFilterCondition.EQUAL,
                        value = spaceManager.get()
                    ),
                    DVFilter(
                        relation = Relations.ID,
                        condition = DVFilterCondition.NOT_EMPTY
                    )
                ),
                keys = listOf(
                    Relations.ID,
                    Relations.SPACE_ID,
                    Relations.TYPE_UNIQUE_KEY,
                    Relations.NAME,
                    Relations.ICON_EMOJI,
                    Relations.ICON_IMAGE,
                    Relations.ICON_OPTION,
                    Relations.COVER_ID,
                    Relations.COVER_TYPE,
                    Relations.IS_ARCHIVED,
                    Relations.IS_DELETED,
                    Relations.CREATED_DATE,
                    Relations.TARGET_OBJECT_TYPE,
                    Relations.TYPE
                ),
            )
            storage.subscribe(params)
        }
    }

    override suspend fun unsubscribeFromTemplates(subId: Id) {
        storage.unsubscribe(listOf(subId))
    }
}