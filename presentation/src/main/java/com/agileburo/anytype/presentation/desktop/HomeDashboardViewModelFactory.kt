package com.agileburo.anytype.presentation.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.agileburo.anytype.domain.auth.interactor.GetCurrentAccount
import com.agileburo.anytype.domain.block.interactor.DragAndDrop
import com.agileburo.anytype.domain.config.GetConfig
import com.agileburo.anytype.domain.dashboard.interactor.CloseDashboard
import com.agileburo.anytype.domain.dashboard.interactor.ObserveHomeDashboard
import com.agileburo.anytype.domain.dashboard.interactor.OpenDashboard
import com.agileburo.anytype.domain.event.interactor.ObserveEvents
import com.agileburo.anytype.domain.image.LoadImage
import com.agileburo.anytype.domain.page.CreatePage

class HomeDashboardViewModelFactory(
    private val getCurrentAccount: GetCurrentAccount,
    private val loadImage: LoadImage,
    private val openDashboard: OpenDashboard,
    private val closeDashboard: CloseDashboard,
    private val createPage: CreatePage,
    private val getConfig: GetConfig,
    private val observeHomeDashboard: ObserveHomeDashboard,
    private val dnd: DragAndDrop,
    private val observeEvents: ObserveEvents
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HomeDashboardViewModel(
            getCurrentAccount = getCurrentAccount,
            loadImage = loadImage,
            openDashboard = openDashboard,
            closeDashboard = closeDashboard,
            createPage = createPage,
            getConfig = getConfig,
            observeHomeDashboard = observeHomeDashboard,
            dragAndDrop = dnd,
            observeEvents = observeEvents
        ) as T
    }
}