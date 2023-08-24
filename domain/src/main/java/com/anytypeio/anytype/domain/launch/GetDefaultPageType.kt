package com.anytypeio.anytype.domain.launch

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import javax.inject.Inject

class GetDefaultPageType @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val blockRepository: BlockRepository,
    private val workspaceManager: WorkspaceManager,
    private val spaceManager: SpaceManager,
    private val configStorage: ConfigStorage,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, GetDefaultPageType.Response>(dispatchers.io) {

    override suspend fun doWork(params: Unit): Response {
        val workspaceId = workspaceManager.getCurrentWorkspace()
        userSettingsRepository.getDefaultObjectType().first?.let {
            val item = searchObjectByIdAndWorkspaceId(it, workspaceId)
            if (item != null) {
                return Response(item.id, item.name)
            } else {
                return searchNote()
            }
        } ?: run {
            return searchNote()
        }
    }

    private suspend fun searchNote(): Response {
        val items = blockRepository.searchObjects(
            limit = 1,
            fulltext = "",
            filters = buildList {
                add(
                    DVFilter(
                        relation = Relations.UNIQUE_KEY,
                        condition = DVFilterCondition.EQUAL,
                        value = ObjectTypeIds.NOTE
                    )
                )
                val space = spaceManager.get().ifEmpty {
                    // Fallback to default space.
                    configStorage.getOrNull()?.space
                }
                if (!space.isNullOrEmpty()) {
                    add(
                        DVFilter(
                            relation = Relations.SPACE_ID,
                            condition = DVFilterCondition.EQUAL,
                            value = space
                        )
                    )
                }
            },
            offset = 0,
            sorts = emptyList(),
            keys = listOf(
                Relations.ID,
                Relations.NAME,
                Relations.UNIQUE_KEY,
                Relations.SPACE_ID
            )
        )
        val note = if (items.isNotEmpty()) {
            ObjectWrapper.Type(items.first())
        } else {
            null
        }
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
            sorts = emptyList(),
            keys = listOf(
                Relations.ID,
                Relations.NAME,
                Relations.UNIQUE_KEY,
                Relations.SPACE_ID
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