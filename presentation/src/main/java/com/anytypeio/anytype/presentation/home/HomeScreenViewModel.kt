package com.anytypeio.anytype.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.search.ObjectSearchSubscriptionContainer
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.TreeWidgetBranchStateHolder
import com.anytypeio.anytype.presentation.widgets.TreeWidgetContainer
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.WidgetDispatchEvent
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.presentation.widgets.parseWidgets
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeScreenViewModel(
    private val configStorage: ConfigStorage,
    private val openObject: OpenObject,
    private val createWidget: CreateWidget,
    private val objectSearchSubscriptionContainer: ObjectSearchSubscriptionContainer,
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val dispatcher: Dispatcher<WidgetDispatchEvent>
) : BaseViewModel() {

    val obj = MutableSharedFlow<ObjectView>()
    val views = MutableStateFlow<List<WidgetView>>(emptyList())

    private val widgets = MutableStateFlow<List<Widget>>(emptyList())
    private val containers = MutableStateFlow<List<TreeWidgetContainer>>(emptyList())
    private val expanded = TreeWidgetBranchStateHolder()

    init {

        viewModelScope.launch {
            obj.map { o ->
                o.blocks.parseWidgets(
                    root = o.root,
                    details = o.details
                )
            }.collect {
                widgets.value = it
            }
        }

        viewModelScope.launch {
            widgets.map {
                it.map { w ->
                    when (w) {
                        is Widget.Link -> TODO()
                        is Widget.Tree -> {
                            TreeWidgetContainer(
                                widget = w,
                                container = objectSearchSubscriptionContainer,
                                expandedBranches = expanded.stream(w.id)
                            )
                        }
                    }
                }
            }.collect {
                containers.value = it
            }
        }

        viewModelScope.launch {
            containers.flatMapLatest {
                combine(
                    it.map { m -> m.view }
                ) { array ->
                    array.toList()
                }
            }.flowOn(appCoroutineDispatchers.io).collect {
                Timber.d("Views update: $it")
                views.value = it + listOf(
                    WidgetView.Action.EditWidgets,
                    WidgetView.Action.CreateWidget,
                    WidgetView.Action.Refresh
                )
            }
        }

        proceedWithOpeningObject()
        proceedWithDispatches()
    }

    private fun proceedWithOpeningObject() {
        viewModelScope.launch {
            val config = configStorage.get()
            openObject(config.widgets).flowOn(appCoroutineDispatchers.io).collect { result ->
                when (result) {
                    is Resultat.Failure -> {
                        Timber.e(result.exception, "Error while opening object.")
                    }
                    is Resultat.Loading -> {
                        // Do nothing.
                    }
                    is Resultat.Success -> {
                        Timber.d("Object view on start:\n${result.value}")
                        obj.emit(result.value)
                    }
                }
            }
        }
    }

    private fun proceedWithDispatches() {
        viewModelScope.launch {
            dispatcher.flow().collect { dispatch ->
                when (dispatch) {
                    is WidgetDispatchEvent.SourcePicked -> {
                        proceedWithCreatingWidget(source = dispatch.source)
                    }
                }
            }
        }
    }

    private fun proceedWithCreatingWidget(source: Id) {
        viewModelScope.launch {
            val config = configStorage.get()
            createWidget(
                CreateWidget.Params(
                    ctx = config.widgets,
                    source = source
                )
            ).collect { s ->
                Timber.d("Status while creating widget: $s")
            }
        }
    }

    @Deprecated("For debugging only")
    fun onRefresh() {
        proceedWithOpeningObject()
    }

    fun onStart() {
        Timber.d("onStart")
    }

    fun onExpand(path: TreePath) {
        expanded.onExpand(linkPath = path)
    }

    class Factory @Inject constructor(
        private val configStorage: ConfigStorage,
        private val openObject: OpenObject,
        private val createWidget: CreateWidget,
        private val objectSearchSubscriptionContainer: ObjectSearchSubscriptionContainer,
        private val appCoroutineDispatchers: AppCoroutineDispatchers,
        private val dispatcher: Dispatcher<WidgetDispatchEvent>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeScreenViewModel(
            configStorage = configStorage,
            openObject = openObject,
            createWidget = createWidget,
            objectSearchSubscriptionContainer = objectSearchSubscriptionContainer,
            appCoroutineDispatchers = appCoroutineDispatchers,
            dispatcher = dispatcher
        ) as T
    }
}