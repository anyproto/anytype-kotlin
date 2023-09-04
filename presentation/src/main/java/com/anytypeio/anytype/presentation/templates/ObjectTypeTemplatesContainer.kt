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
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

sealed interface ObjectTypeTemplatesContainer {

    suspend fun subscribe(type: Id?): Flow<List<ObjectWrapper.Basic>>
    suspend fun unsubscribe()
}

class DefaultObjectTypeTemplatesContainer(
    private val storage: StorelessSubscriptionContainer,
    private val workspaceManager: WorkspaceManager
) : ObjectTypeTemplatesContainer {

    override suspend fun subscribe(type: Id?): Flow<List<ObjectWrapper.Basic>> {
        return if (type.isNullOrBlank()) {
            emptyFlow()
        } else {
            val params = StoreSearchParams(
                subscription = TYPE_TEMPLATES_SUBSCRIPTION_ID,
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
                        relation = Relations.TYPE,
                        condition = DVFilterCondition.EQUAL,
                        value = ObjectTypeIds.TEMPLATE
                    ),
                    DVFilter(
                        relation = Relations.TARGET_OBJECT_TYPE,
                        condition = DVFilterCondition.EQUAL,
                        value = type
                    ),
                    DVFilter(
                        relation = Relations.WORKSPACE_ID,
                        condition = DVFilterCondition.EQUAL,
                        value = workspaceManager.getCurrentWorkspace()
                    ),
                    DVFilter(
                        relation = Relations.ID,
                        condition = DVFilterCondition.NOT_EMPTY
                    )
                ),
                keys = listOf(
                    Relations.ID,
                    Relations.NAME,
                    Relations.LAYOUT,
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

    override suspend fun unsubscribe() {
        storage.unsubscribe(listOf(TYPE_TEMPLATES_SUBSCRIPTION_ID))
    }

    companion object {
        const val TYPE_TEMPLATES_SUBSCRIPTION_ID = "object-type-templates-subscription"
    }
}