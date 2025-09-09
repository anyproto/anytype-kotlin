package com.anytypeio.anytype.domain.spaces

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.SpaceCreationUseCase
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class CreateSpace @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreateSpace.Params, Command.CreateSpace.Result>(dispatchers.io) {

    override suspend fun doWork(params: Params) = repo.createWorkspace(
        command = Command.CreateSpace(
            details = params.details,
            useCase = params.useCase
        )
    )

    data class Params(
        val details: Struct,
        val useCase: SpaceCreationUseCase = SpaceCreationUseCase.GET_STARTED_MOBILE
    )
}