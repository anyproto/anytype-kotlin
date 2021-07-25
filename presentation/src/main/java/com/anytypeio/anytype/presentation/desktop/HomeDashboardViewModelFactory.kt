package com.anytypeio.anytype.presentation.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.auth.interactor.GetProfile
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.config.GetDebugSettings
import com.anytypeio.anytype.domain.config.GetFlavourConfig
import com.anytypeio.anytype.domain.dashboard.interactor.*
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.CreatePage

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
    private val searchArchivedObjects: SearchArchivedObjects,
    private val searchRecentObjects: SearchRecentObjects,
    private val searchInboxObjects: SearchInboxObjects,
    private val searchObjectSets: SearchObjectSets,
    private val getFlavourConfig: GetFlavourConfig,
    private val urlBuilder: UrlBuilder
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
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
            searchArchivedObjects = searchArchivedObjects,
            searchRecentObjects = searchRecentObjects,
            searchInboxObjects = searchInboxObjects,
            searchObjectSets = searchObjectSets,
            getFlavourConfig = getFlavourConfig,
            urlBuilder = urlBuilder
        ) as T
    }
}