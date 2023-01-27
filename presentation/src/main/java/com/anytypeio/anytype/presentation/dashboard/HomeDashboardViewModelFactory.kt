package com.anytypeio.anytype.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.domain.auth.interactor.GetProfile
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.config.GetDebugSettings
import com.anytypeio.anytype.domain.dashboard.interactor.CloseDashboard
import com.anytypeio.anytype.domain.dashboard.interactor.OpenDashboard
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.DeleteObjects
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.search.CancelSearchSubscription
import com.anytypeio.anytype.domain.search.ObjectSearchSubscriptionContainer
import com.anytypeio.anytype.domain.workspace.WorkspaceManager

class HomeDashboardViewModelFactory(
    private val getProfile: GetProfile,
    private val openDashboard: OpenDashboard,
    private val closeDashboard: CloseDashboard,
    private val getConfig: GetConfig,
    private val move: Move,
    private val interceptEvents: InterceptEvents,
    private val eventConverter: HomeDashboardEventConverter,
    private val getDebugSettings: GetDebugSettings,
    private val analytics: Analytics,
    private val urlBuilder: UrlBuilder,
    private val setObjectListIsArchived: SetObjectListIsArchived,
    private val deleteObjects: DeleteObjects,
    private val objectSearchSubscriptionContainer: ObjectSearchSubscriptionContainer,
    private val cancelSearchSubscription: CancelSearchSubscription,
    private val objectStore: ObjectStore,
    private val createObject: CreateObject,
    private val workspaceManager: WorkspaceManager,
    private val machine: HomeDashboardStateMachine.Interactor,
    private val featureToggles: FeatureToggles
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeDashboardViewModel(
            getProfile = getProfile,
            openDashboard = openDashboard,
            closeDashboard = closeDashboard,
            getConfig = getConfig,
            move = move,
            interceptEvents = interceptEvents,
            eventConverter = eventConverter,
            getDebugSettings = getDebugSettings,
            analytics = analytics,
            urlBuilder = urlBuilder,
            deleteObjects = deleteObjects,
            setObjectListIsArchived = setObjectListIsArchived,
            objectSearchSubscriptionContainer = objectSearchSubscriptionContainer,
            cancelSearchSubscription = cancelSearchSubscription,
            objectStore = objectStore,
            createObject = createObject,
            workspaceManager = workspaceManager,
            favoriteObjectStateMachine = machine,
            featureToggles = featureToggles
        ) as T
    }
}