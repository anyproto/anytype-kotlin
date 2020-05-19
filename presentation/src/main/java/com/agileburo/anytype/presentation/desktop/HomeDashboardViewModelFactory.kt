package com.agileburo.anytype.presentation.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.auth.interactor.GetCurrentAccount
import com.agileburo.anytype.domain.block.interactor.DragAndDrop
import com.agileburo.anytype.domain.config.GetConfig
import com.agileburo.anytype.domain.dashboard.interactor.CloseDashboard
import com.agileburo.anytype.domain.dashboard.interactor.OpenDashboard
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.page.CreatePage

class HomeDashboardViewModelFactory(
    private val getCurrentAccount: GetCurrentAccount,
    private val openDashboard: OpenDashboard,
    private val closeDashboard: CloseDashboard,
    private val createPage: CreatePage,
    private val getConfig: GetConfig,
    private val dnd: DragAndDrop,
    private val interceptEvents: InterceptEvents,
    private val eventConverter: HomeDashboardEventConverter
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HomeDashboardViewModel(
            getCurrentAccount = getCurrentAccount,
            openDashboard = openDashboard,
            closeDashboard = closeDashboard,
            createPage = createPage,
            getConfig = getConfig,
            dragAndDrop = dnd,
            interceptEvents = interceptEvents,
            eventConverter = eventConverter
        ) as T
    }
}