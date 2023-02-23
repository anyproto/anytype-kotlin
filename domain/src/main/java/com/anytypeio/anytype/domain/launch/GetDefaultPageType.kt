package com.anytypeio.anytype.domain.launch

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.workspace.WorkspaceManager

class GetDefaultPageType(
    private val userSettingsRepository: UserSettingsRepository,
    private val blockRepository: BlockRepository,
    private val workspaceManager: WorkspaceManager,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, GetDefaultPageType.Response>(dispatchers.io) {

    override suspend fun doWork(params: Unit): Response {
        val workspaceId = workspaceManager.getCurrentWorkspace()
        userSettingsRepository.getDefaultObjectType().first?.let {
            val item = searchObjectByIdAndWorkspaceId(it, workspaceId)
            if (item != null) {
                return Response(item.id, item.name)
            } else {
                return searchNote(workspaceId)
            }
        } ?: run {
            return searchNote(workspaceId)
        }
    }

    private suspend fun searchNote(workspaceId: Id): Response {
        val note = searchObjectByIdAndWorkspaceId(
            ObjectTypeIds.NOTE,
            workspaceId
        )
        return Response(note?.id, note?.name)
    }

    private suspend fun searchObjectByIdAndWorkspaceId(
        id: String,
        workspaceId: String
    ): ObjectWrapper.Type? {
        val items = blockRepository.searchObjects(
            limit = 1,
            fulltext = "",
            filters = buildList {
                addAll(filterObjectTypeLibrary(workspaceId))
                add(
                    DVFilter(
                        relation = Relations.ID,
                        condition = DVFilterCondition.EQUAL,
                        value = id
                    )
                )
            },
            offset = 0,
            sorts = listOf(),
            keys = listOf(
                Relations.ID,
                Relations.NAME,
            )
        )
        return if (items.isNotEmpty()) {
            ObjectWrapper.Type(items.first())
        } else {
            null
        }
    }

    class Response(val type: String?, val name: String?)

    private fun filterObjectTypeLibrary(workspaceId: String) = listOf(
        DVFilter(
            relation = Relations.TYPE,
            condition = DVFilterCondition.EQUAL,
            value = ObjectTypeIds.OBJECT_TYPE
        ),
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
            relation = Relations.IS_HIDDEN,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.WORKSPACE_ID,
            condition = DVFilterCondition.EQUAL,
            value = workspaceId
        )
    )

}