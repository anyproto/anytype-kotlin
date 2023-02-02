package com.anytypeio.anytype.presentation.widgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.search.ObjectSearchViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SelectWidgetSourceViewModel(
    private val urlBuilder: UrlBuilder,
    private val searchObjects: SearchObjects,
    private val getObjectTypes: GetObjectTypes,
    private val analytics: Analytics,
    private val workspaceManager: WorkspaceManager,
    private val dispatcher: Dispatcher<WidgetDispatchEvent>
) : ObjectSearchViewModel(
    urlBuilder = urlBuilder,
    searchObjects = searchObjects,
    getObjectTypes = getObjectTypes,
    analytics = analytics,
    workspaceManager = workspaceManager
) {

    val isDismissed = MutableStateFlow(false)

    fun onStart() {
        getObjectTypes()
        startProcessingSearchQuery(null)
    }

    override fun onObjectClicked(view: DefaultObjectView) {
        viewModelScope.launch {
            dispatcher.send(WidgetDispatchEvent.SourcePicked(view.id))
            isDismissed.value = true
        }
    }

    class Factory(
        private val urlBuilder: UrlBuilder,
        private val searchObjects: SearchObjects,
        private val getObjectTypes: GetObjectTypes,
        private val analytics: Analytics,
        private val workspaceManager: WorkspaceManager,
        private val dispatcher: Dispatcher<WidgetDispatchEvent>
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SelectWidgetSourceViewModel(
                urlBuilder = urlBuilder,
                searchObjects = searchObjects,
                analytics = analytics,
                workspaceManager = workspaceManager,
                getObjectTypes = getObjectTypes,
                dispatcher = dispatcher
            ) as T
        }
    }
}