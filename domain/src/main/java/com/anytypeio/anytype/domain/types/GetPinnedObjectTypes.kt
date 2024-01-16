package com.anytypeio.anytype.domain.types

import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.FlowInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow

class GetPinnedObjectTypes @Inject constructor(
    private val repo: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : FlowInteractor<GetPinnedObjectTypes.Params, List<TypeId>>(dispatchers.io) {

    override fun build(): Flow<List<TypeId>> {
        throw UnsupportedOperationException()
    }

    override fun build(params: Params) = repo.getPinnedObjectTypes(
        space = params.space
    ).catch { emit(emptyList()) }

    class Params(val space: SpaceId)
}