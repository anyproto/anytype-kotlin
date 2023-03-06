package com.anytypeio.anytype.domain.bin

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.workspace.WorkspaceManager

class EmptyBin(
    private val repo: BlockRepository,
    private val workspaceManager: WorkspaceManager,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, List<Id>>(dispatchers.io) {
    override suspend fun doWork(params: Unit) : List<Id> {
        val archived = repo.searchObjects(
            filters = listOf(
                DVFilter(
                    relation = Relations.IS_ARCHIVED,
                    condition = DVFilterCondition.EQUAL,
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
                    value = workspaceManager.getCurrentWorkspace()
                )
            ),
            sorts = emptyList(),
            fulltext = "",
            limit = 0,
            offset = 0
        ).map { struct ->
            ObjectWrapper.Basic(struct).id
        }
        repo.deleteObjects(
            targets = archived
        )
        return archived
    }
}