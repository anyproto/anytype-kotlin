package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.GlobalSearchHistory
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

class SearchWithMeta @Inject constructor(
    private val repo: BlockRepository,
    private val settings: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SearchWithMeta.Params, List<Command.SearchWithMeta.Result>>(dispatchers.io) {
    override suspend fun doWork(params: SearchWithMeta.Params): List<Command.SearchWithMeta.Result> {
        return repo.searchObjectWithMeta(command = params.command).also {
            if (params.saveSearch) saveSearch(params)
        }
    }

    private suspend fun saveSearch(params: Params) {
        val search = GlobalSearchHistory(
            query = params.command.query,
            relatedObject = params.relatedObjectId
        )
        settings.setGlobalSearchHistory(globalSearchHistory = search, space = params.command.space)
    }

    data class Params(
        val command: Command.SearchWithMeta,
        val relatedObjectId: Id?,
        val saveSearch: Boolean = false
    )
}