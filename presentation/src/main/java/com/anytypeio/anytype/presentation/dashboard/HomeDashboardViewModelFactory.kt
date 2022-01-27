package com.anytypeio.anytype.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.auth.interactor.GetProfile
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.config.FlavourConfigProvider
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.config.GetDebugSettings
import com.anytypeio.anytype.domain.dashboard.interactor.CloseDashboard
import com.anytypeio.anytype.domain.dashboard.interactor.OpenDashboard
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.DeleteObjects
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.page.CreatePage
import com.anytypeio.anytype.domain.search.CancelSearchSubscription
import com.anytypeio.anytype.domain.search.ObjectSearchSubscriptionContainer

class HomeDashboardViewModelFactory(
    private val getProfile: GetProfile,
    private val openDashboard: OpenDashboard,
    private val closeDashboard: CloseDashboard,
    private val createPage: CreatePage,
    private val getConfig: GetConfig,
    private val move: Move,
    private val interceptEvents: InterceptEvents,
    private val eventConverter: HomeDashboardEventConverter,
    private val getDebugSettings: GetDebugSettings,
    private val analytics: Analytics,
    private val searchObjects: SearchObjects,
    private val getDefaultEditorType: GetDefaultEditorType,
    private val urlBuilder: UrlBuilder,
    private val setObjectListIsArchived: SetObjectListIsArchived,
    private val deleteObjects: DeleteObjects,
    private val flavourConfigProvider: FlavourConfigProvider,
    private val objectSearchSubscriptionContainer: ObjectSearchSubscriptionContainer,
    private val cancelSearchSubscription: CancelSearchSubscription,
    private val objectStore: ObjectStore
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeDashboardViewModel(
            getProfile = getProfile,
            openDashboard = openDashboard,
            closeDashboard = closeDashboard,
            createPage = createPage,
            getConfig = getConfig,
            move = move,
            interceptEvents = interceptEvents,
            eventConverter = eventConverter,
            getDebugSettings = getDebugSettings,
            analytics = analytics,
            searchObjects = searchObjects,
            urlBuilder = urlBuilder,
            getDefaultEditorType = getDefaultEditorType,
            deleteObjects = deleteObjects,
            setObjectListIsArchived = setObjectListIsArchived,
            flavourConfigProvider = flavourConfigProvider,
            objectSearchSubscriptionContainer = objectSearchSubscriptionContainer,
            cancelSearchSubscription = cancelSearchSubscription,
            objectStore = objectStore
        ) as T
    }
}