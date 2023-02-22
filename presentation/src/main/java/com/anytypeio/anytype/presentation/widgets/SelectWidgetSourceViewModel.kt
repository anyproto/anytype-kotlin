package com.anytypeio.anytype.presentation.widgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.search.ObjectSearchViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

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
    var config : Config = Config.None

    init {
        viewModelScope.launch {
            dispatcher.flow()
                .filterIsInstance<WidgetDispatchEvent.TypePicked>()
                .take(1)
                .collect {
                    isDismissed.value = true
                }
        }
    }

    fun onStartWithNewWidget() {
        Timber.d("onStart with picking source for new widget")
        config = Config.NewWidget
        proceedWithSearchQuery()
    }

    fun onStartWithExistingWidget(
        ctx: Id,
        widget: Id,
        source: Id,
        type: Int
    ) {
        Timber.d("onStart with picking source for an existing widget")
        config = Config.ExistingWidget(
            ctx = ctx,
            widget = widget,
            source = source,
            type = type
        )
        proceedWithSearchQuery()
    }

    private fun proceedWithSearchQuery() {
        getObjectTypes()
        startProcessingSearchQuery(null)
    }

    override fun onObjectClicked(view: DefaultObjectView) {
        Timber.d("onObjectClicked, view:[$view]")
        when(val curr = config) {
            is Config.NewWidget -> {
                viewModelScope.launch {
                    dispatcher.send(
                        WidgetDispatchEvent.SourcePicked(
                            source = view.id,
                            sourceLayout = view.layout?.code ?: -1
                        )
                    )
                }
            }
            is Config.ExistingWidget -> {
                viewModelScope.launch {
                    dispatcher.send(
                        WidgetDispatchEvent.SourceChanged(
                            ctx = curr.ctx,
                            widget = curr.widget,
                            source = view.id,
                            type = curr.type
                        )
                    )
                    isDismissed.value = true
                }
            }
            is Config.None -> {
                // Do nothing.
            }
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

    sealed class Config {
        object None: Config()
        object NewWidget : Config()
        data class ExistingWidget(
            val ctx: Id,
            val widget: Id,
            val source: Id,
            val type: Int
        ) : Config()
    }
}