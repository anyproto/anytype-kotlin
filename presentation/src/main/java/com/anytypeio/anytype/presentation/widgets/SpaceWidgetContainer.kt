package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.UNKNOWN_SPACE_TYPE
import com.anytypeio.anytype.core_models.asSpaceType
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer.Store
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.spaces.spaceIcon
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart

class SpaceWidgetContainer @Inject constructor(
    private val spaceManager: SpaceManager,
    private val container: StorelessSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    private val members: ActiveSpaceMemberSubscriptionContainer,
    private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer
) : WidgetContainer {

    override val view: Flow<WidgetView> = buildFlow()

    private fun buildFlow() : Flow<WidgetView> {
        return spaceManager.observe().flatMapLatest { config ->
            combine(
                container.subscribe(
                    StoreSearchByIdsParams(
                        space = SpaceId(config.techSpace),
                        subscription = SPACE_WIDGET_SUBSCRIPTION,
                        targets = listOf(config.spaceView),
                        keys = buildList {
                            addAll(ObjectSearchConstants.defaultKeys)
                            add(Relations.CHAT_ID)
                            add(Relations.SPACE_ACCESS_TYPE)
                            add(Relations.SPACE_UX_TYPE)
                            add(Relations.ICON_OPTION)
                        }
                    )
                ).mapNotNull { results ->
                      if (results.isNotEmpty())
                          ObjectWrapper.SpaceView(results.first().map)
                    else
                        null
                }.onStart {
                    val cached = spaceViewSubscriptionContainer.get(SpaceId(config.space))
                    if (cached != null) {
                        emit(cached)
                    }
                },
                members.observe(SpaceId(config.space)).map { store ->
                    when (store) {
                        is Store.Empty -> 0
                        is Store.Data -> store.members.count { member ->
                            member.status == ParticipantStatus.ACTIVE
                        }
                    }
                }
            ) { spaceView, membersCount ->
                WidgetView.SpaceWidget.View(
                    space = spaceView,
                    icon = spaceView.spaceIcon(urlBuilder),
                    type = spaceView.spaceAccessType?.asSpaceType() ?: UNKNOWN_SPACE_TYPE,
                    membersCount = membersCount
                )
            }
        }
    }

    companion object {
        const val SPACE_WIDGET_SUBSCRIPTION = "subscription.home.space-widget"
    }
}