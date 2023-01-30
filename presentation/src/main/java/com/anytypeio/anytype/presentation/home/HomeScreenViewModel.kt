package com.anytypeio.anytype.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.search.ObjectSearchSubscriptionContainer
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.TreeWidgetBranchStateHolder
import com.anytypeio.anytype.presentation.widgets.TreeWidgetContainer
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.presentation.widgets.parseWidgets
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
    private val dispatchers: AppCoroutineDispatchers
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
            }.flowOn(dispatchers.io).collect {
                Timber.d("Views update: $it")
                views.value = it
            }
        }

        viewModelScope.launch {
            val config = configStorage.get()
            openObject(config.widgets).collect { result ->
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

//        proceedWithCreatingWidget()
    }

    private fun proceedWithCreatingWidget() {
        viewModelScope.launch {
            val config = configStorage.get()
            createWidget(
                CreateWidget.Params(
                    ctx = config.widgets,
                    source = "bafybbsj5xhyf7yvaakfd5bdjqjowp7cjzi4cqxcunfy7ejf4apmowk6u"
                )
            ).collect { s ->
                Timber.d("Status while creating widget: $s")
            }
        }
    }

    fun onStart() {
        Timber.d("onStart")
    }

    fun onExpand(path: TreePath) {
        expanded.onExpand(linkPath = path)
    }

    class Factory(
        private val configStorage: ConfigStorage,
        private val openObject: OpenObject,
        private val createWidget: CreateWidget,
        private val objectSearchSubscriptionContainer: ObjectSearchSubscriptionContainer,
        private val dispatchers: AppCoroutineDispatchers
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeScreenViewModel(
            configStorage = configStorage,
            openObject = openObject,
            createWidget = createWidget,
            objectSearchSubscriptionContainer = objectSearchSubscriptionContainer,
            dispatchers = dispatchers
        ) as T
    }
}