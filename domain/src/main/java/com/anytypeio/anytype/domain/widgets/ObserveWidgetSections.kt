package com.anytypeio.anytype.domain.widgets

import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.FlowInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.core_models.WidgetSections
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

/**
 * Use-case for observing widget sections configuration for a space.
 * Returns the order and visibility settings for home screen sections.
 */
class ObserveWidgetSections @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : FlowInteractor<ObserveWidgetSections.Params, WidgetSections>(dispatchers.io) {

    override fun build(params: Params): Flow<WidgetSections> {
        return userSettingsRepository
            .observeWidgetSections(params.spaceId)
            .catch { emit(WidgetSections.default()) }
            .flowOn(dispatchers.io)
    }

    data class Params(val spaceId: SpaceId)
}
