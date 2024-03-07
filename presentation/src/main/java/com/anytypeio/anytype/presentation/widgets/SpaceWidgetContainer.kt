package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.PERSONAL_SPACE_TYPE
import com.anytypeio.anytype.core_models.PRIVATE_SPACE_TYPE
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SHARED_SPACE_TYPE
import com.anytypeio.anytype.core_models.UNKNOWN_SPACE_TYPE
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.spaces.spaceIcon
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class SpaceWidgetContainer @Inject constructor(
    private val spaceManager: SpaceManager,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val spaceGradientProvider: SpaceGradientProvider,
    private val urlBuilder: UrlBuilder
) : WidgetContainer {

    override val view: Flow<WidgetView> = buildFlow()

    private fun buildFlow() = spaceManager
        .observe()
        .flatMapLatest { config ->
            storelessSubscriptionContainer.subscribe(
                StoreSearchByIdsParams(
                    subscription = SPACE_WIDGET_SUBSCRIPTION,
                    targets = listOf(config.spaceView),
                    keys = buildList {
                        addAll(ObjectSearchConstants.defaultKeys)
                        add(Relations.SPACE_ACCESS_TYPE)
                        add(Relations.ICON_OPTION)
                    }
                )
            ).map { results ->
                config to results
            }
        }.mapNotNull { (config, results) ->
            val spaceObject = results.firstOrNull()
            if (spaceObject != null) {
                val wrapper = ObjectWrapper.SpaceView(spaceObject.map)
                WidgetView.SpaceWidget.View(
                    space = spaceObject,
                    icon = spaceObject.spaceIcon(
                        builder = urlBuilder,
                        spaceGradientProvider = spaceGradientProvider
                    ),
                    type = when(wrapper.spaceAccessType) {
                        SpaceAccessType.PRIVATE -> PRIVATE_SPACE_TYPE
                        SpaceAccessType.PERSONAL -> PERSONAL_SPACE_TYPE
                        SpaceAccessType.SHARED -> SHARED_SPACE_TYPE
                        else -> UNKNOWN_SPACE_TYPE
                    }
                )
            } else {
                null
            }
        }

    companion object {
        const val SPACE_WIDGET_SUBSCRIPTION = "subscription.home.space-widget"
    }
}