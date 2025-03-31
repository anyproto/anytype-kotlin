package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.Subscriptions
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

@Deprecated("To be deleted in favor of system bin widget.")
class SpaceBinWidgetContainer @Inject constructor(
    private val manager: SpaceManager,
    private val container: StorelessSubscriptionContainer
) : WidgetContainer {

    override val view: Flow<WidgetView>
        get() = manager
            .observe()
            .flatMapLatest { config ->
                container.subscribe(
                    searchParams = StoreSearchParams(
                        space = SpaceId(config.space),
                        subscription = Subscriptions.SUBSCRIPTION_BIN,
                        filters = ObjectSearchConstants.filterTabArchive(),
                        sorts = emptyList(),
                        limit = 1,
                        keys = listOf(Relations.ID)
                    )
                ).map { result ->
                    WidgetView.Bin(
                        id = Subscriptions.SUBSCRIPTION_BIN,
                        isEmpty = result.isEmpty(),
                        isLoading = false
                    )
                }
            }.onStart {
                emit(
                    WidgetView.Bin(
                        id = Subscriptions.SUBSCRIPTION_BIN,
                        isEmpty = true,
                        isLoading = true
                    )
                )
            }
}