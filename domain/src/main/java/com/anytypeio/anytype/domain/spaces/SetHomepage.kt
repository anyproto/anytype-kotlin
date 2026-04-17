package com.anytypeio.anytype.domain.spaces

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class SetHomepage @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository
) : ResultInteractor<SetHomepage.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        repo.setHomepage(
            Command.SetHomepage(
                spaceId = params.spaceId,
                homepage = params.homepage
            )
        )
    }

    data class Params(val spaceId: Id, val homepage: Id)
}
