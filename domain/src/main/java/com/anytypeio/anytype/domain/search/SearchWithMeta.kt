package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

class SearchWithMeta @Inject constructor(
    private val repo: BlockRepository,
    private val dispatchers: AppCoroutineDispatchers,
    private val settings: UserSettingsRepository
) : ResultInteractor<Command.SearchWithMeta, List<Command.SearchWithMeta.Result>>(dispatchers.io) {
    override suspend fun doWork(params: Command.SearchWithMeta): List<Command.SearchWithMeta.Result> {
        return repo.searchObjectWithMeta(command = params).also {
            settings.setLastSearchQuery(query = params.query, space = params.space)
        }
    }
}