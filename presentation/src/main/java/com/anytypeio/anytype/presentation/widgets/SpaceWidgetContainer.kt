package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Relations
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
                        add(Relations.ICON_OPTION)
                    }
                )
            )
        }.mapNotNull { results ->
            val spaceObject = results.firstOrNull()
            if (spaceObject != null) {
                WidgetView.SpaceWidget.View(
                    space = spaceObject,
                    icon = spaceObject.spaceIcon(
                        builder = urlBuilder,
                        spaceGradientProvider = spaceGradientProvider
                    )
                )
            } else {
                null
            }
        }

    companion object {
        const val SPACE_WIDGET_SUBSCRIPTION = "subscription.home.space-widget"
    }
}