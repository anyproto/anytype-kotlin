package com.anytypeio.anytype.presentation.widgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.getOrDefault
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.extension.sendChangeWidgetSourceEvent
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.navigation.NewObject
import com.anytypeio.anytype.presentation.search.ObjectSearchSection
import com.anytypeio.anytype.presentation.search.ObjectSearchView
import com.anytypeio.anytype.presentation.search.ObjectSearchViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.widgets.source.BundledWidgetSourceView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectWidgetSourceViewModel(
    private val vmParams: VmParams,
    private val urlBuilder: UrlBuilder,
    private val searchObjects: SearchObjects,
    private val getObjectTypes: GetObjectTypes,
    private val analytics: Analytics,
    private val dispatcher: Dispatcher<WidgetDispatchEvent>,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
) : ObjectSearchViewModel(
    vmParams = vmParams,
    urlBuilder = urlBuilder,
    searchObjects = searchObjects,
    getObjectTypes = getObjectTypes,
    analytics = analytics,
    analyticSpaceHelperDelegate = analyticSpaceHelperDelegate
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

    override fun resolveViews(result: Resultat<List<DefaultObjectView>>) {
        result.fold(
            onSuccess = { views ->
                if (views.isEmpty()) {
                    stateData.postValue(ObjectSearchView.NoResults(userInput.value))
                } else {
                    if (userInput.value.isEmpty()) {
                        stateData.postValue(
                            ObjectSearchView.Success(
                                buildList {
                                    add(NewObject)
                                    add(ObjectSearchSection.SelectWidgetSource.DefaultLists)
                                    addAll(
                                        listOf(
                                            BundledWidgetSourceView.Favorites,
                                            BundledWidgetSourceView.Sets,
                                            BundledWidgetSourceView.Collections,
                                            BundledWidgetSourceView.Recent,
                                            BundledWidgetSourceView.RecentLocal,
                                        )
                                    )
                                    add(ObjectSearchSection.SelectWidgetSource.FromMyObjects)
                                    addAll(views)
                                }
                            )
                        )
                    } else {
                        stateData.postValue(ObjectSearchView.Success(views))
                    }
                }
            },
            onLoading = {
                stateData.postValue(ObjectSearchView.Loading)
            },
            onFailure = {
                Timber.e(it, "Error while selecting source for widget")
            }
        )
    }

    fun onStartWithNewWidget(
        target: Id?,
        isInEditMode: Boolean
    ) {
        Timber.d("onStart with picking source for new widget")
        config = Config.NewWidget(
            target = target,
            isInEditMode = isInEditMode
        )
        proceedWithSearchQuery()
    }

    fun onStartWithExistingWidget(
        ctx: Id,
        widget: Id,
        source: Id,
        type: Int,
        isInEditMode: Boolean
    ) {
        Timber.d("onStart with picking source for an existing widget")
        config = Config.ExistingWidget(
            ctx = ctx,
            widget = widget,
            source = source,
            type = type,
            isInEditMode = isInEditMode
        )
        proceedWithSearchQuery()
    }

    private fun proceedWithSearchQuery() {
        getObjectTypes()
        startProcessingSearchQuery(null)
    }

    fun onBundledWidgetSourceClicked(view: BundledWidgetSourceView) {
        Timber.d("onBundledWidgetSourceClicked, view:[$view]")
        when (val curr = config) {
            is Config.NewWidget -> {
                viewModelScope.launch {
                    dispatcher.send(
                        WidgetDispatchEvent.SourcePicked.Bundled(
                            source = view.id,
                            target = curr.target
                        )
                    ).also {
                        sendChangeWidgetSourceEvent(
                            analytics = analytics,
                            view = view,
                            isForNewWidget = true,
                            isInEditMode = curr.isInEditMode
                        )
                    }
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
                    ).also {
                        sendChangeWidgetSourceEvent(
                            analytics = analytics,
                            view = view,
                            isForNewWidget = false,
                            isInEditMode = curr.isInEditMode
                        )
                    }
                    isDismissed.value = true
                }
            }
            Config.None -> {
                Timber.w("Missing config for widget source")
            }
        }
    }

    fun onCreateNewObjectClicked() {
        viewModelScope.launch {
            dispatcher.send(WidgetDispatchEvent.NewWithWidgetWithNewSource)
            isDismissed.value = true
        }
    }

    override fun onObjectClicked(view: DefaultObjectView) {
        Timber.d("onObjectClicked, view:[$view]")
        when(val curr = config) {
            is Config.NewWidget -> {
                viewModelScope.launch {
                    dispatcher.send(
                        WidgetDispatchEvent.SourcePicked.Default(
                            source = view.id,
                            sourceLayout = view.layout?.code ?: -1,
                            target = curr.target
                        )
                    ).also {
                        dispatchSelectCustomSourceAnalyticEvent(
                            view = view,
                            isForNewWidget = true,
                            isInEditMode = curr.isInEditMode
                        )
                    }
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
                    ).also {
                        dispatchSelectCustomSourceAnalyticEvent(
                            view = view,
                            isForNewWidget = false,
                            isInEditMode = curr.isInEditMode
                        )
                    }
                    isDismissed.value = true
                }
            }
            is Config.None -> {
                // Do nothing.
            }
        }
    }

    private fun CoroutineScope.dispatchSelectCustomSourceAnalyticEvent(
        view: DefaultObjectView,
        isForNewWidget: Boolean,
        isInEditMode: Boolean
    ) {
        val sourceObjectType = types.value.getOrDefault(emptyList()).find { type ->
            type.id == view.type
        }
        if (sourceObjectType != null) {
            sendChangeWidgetSourceEvent(
                analytics = analytics,
                sourceObjectTypeId = sourceObjectType.sourceObject.orEmpty(),
                isCustomObjectType = sourceObjectType.sourceObject.isNullOrEmpty(),
                isForNewWidget = isForNewWidget,
                isInEditMode = isInEditMode
            )
        } else {
            Timber.e("Could not found type for analytics")
        }
    }

    class Factory(
        private val vmParams: VmParams,
        private val urlBuilder: UrlBuilder,
        private val searchObjects: SearchObjects,
        private val getObjectTypes: GetObjectTypes,
        private val analytics: Analytics,
        private val dispatcher: Dispatcher<WidgetDispatchEvent>,
        private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SelectWidgetSourceViewModel(
                vmParams = vmParams,
                urlBuilder = urlBuilder,
                searchObjects = searchObjects,
                analytics = analytics,
                getObjectTypes = getObjectTypes,
                dispatcher = dispatcher,
                analyticSpaceHelperDelegate = analyticSpaceHelperDelegate
            ) as T
        }
    }

    sealed class Config {
        data object None : Config()
        data class NewWidget(
            val target: Id?,
            val isInEditMode: Boolean
        ) : Config()
        data class ExistingWidget(
            val ctx: Id,
            val widget: Id,
            val source: Id,
            val type: Int,
            val isInEditMode: Boolean
        ) : Config()
    }
}