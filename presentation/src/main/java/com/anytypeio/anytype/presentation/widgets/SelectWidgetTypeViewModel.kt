package com.anytypeio.anytype.presentation.widgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.WidgetLayout
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.widgets.UpdateWidget
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.home.Command
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectWidgetTypeViewModel(
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val payloadDispatcher: Dispatcher<Payload>,
    private val widgetDispatcher: Dispatcher<WidgetDispatchEvent>,
    private val updateWidget: UpdateWidget
) : BaseViewModel() {

    val views = MutableStateFlow(
        listOf(
            WidgetTypeView.Tree(isSelected = false),
            WidgetTypeView.Link(isSelected = false)
        )
    )

    val isDismissed = MutableStateFlow(false)

    fun onStartForExistingWidget(
        currentType: Int,
        sourceLayout: Int,
        source: Id
    ) {
        Timber.d("onStart for existing widget")
        if (BundledWidgetSourceIds.ids.contains(source)) {
            views.value = listOf(
                WidgetTypeView.Tree().setIsSelected(currentType),
                WidgetTypeView.List().setIsSelected(currentType)
            )
        } else {
            val objectLayout = ObjectType.Layout.values().find { it.code == sourceLayout }
            if (objectLayout == ObjectType.Layout.SET || objectLayout == ObjectType.Layout.COLLECTION) {
                views.value = listOf(
                    WidgetTypeView.List().setIsSelected(currentType),
                    WidgetTypeView.Link().setIsSelected(currentType)
                )
            } else {
                views.value = views.value.map { view -> view.setIsSelected(currentType) }
            }
        }
    }

    fun onStartForNewWidget(layout: Int, source: Id) {
        Timber.d("onStart for new widget")
        if (BundledWidgetSourceIds.ids.contains(source)) {
            views.value = listOf(
                WidgetTypeView.Tree(isSelected = false),
                WidgetTypeView.List(isSelected = false)
            )
        } else {
            val objectLayout = ObjectType.Layout.values().find { it.code == layout }
            if (objectLayout == ObjectType.Layout.SET || objectLayout == ObjectType.Layout.COLLECTION) {
                views.value = listOf(
                    WidgetTypeView.List(isSelected = false),
                    WidgetTypeView.Link(isSelected = false)
                )
            }
        }
    }

    fun onWidgetTypeClicked(
        ctx: Id,
        widget: Id,
        source: Id,
        view: WidgetTypeView
    ) {
        if (!view.isSelected) {
            viewModelScope.launch {
                updateWidget(
                    UpdateWidget.Params(
                        ctx = ctx,
                        target = widget,
                        source = source,
                        type = when (view) {
                            is WidgetTypeView.Link -> WidgetLayout.LINK
                            is WidgetTypeView.Tree -> WidgetLayout.TREE
                            is WidgetTypeView.List -> WidgetLayout.LIST
                        }
                    )
                ).flowOn(appCoroutineDispatchers.io).collect { result ->
                    result.fold(
                        onFailure = {
                            Timber.e(it, "Error while updating widget type")
                        },
                        onSuccess = {
                            payloadDispatcher.send(it).also {
                                isDismissed.value = true
                            }
                        }
                    )
                }
            }
        }
    }

    fun onWidgetTypeClicked(
        source: Id,
        target: Id?,
        view: WidgetTypeView
    ) {
        if (!view.isSelected) {
            viewModelScope.launch {
                widgetDispatcher.send(
                    WidgetDispatchEvent.TypePicked(
                        source = source,
                        target = target,
                        widgetType = when (view) {
                            is WidgetTypeView.Link -> Command.ChangeWidgetType.TYPE_LINK
                            is WidgetTypeView.Tree -> Command.ChangeWidgetType.TYPE_TREE
                            is WidgetTypeView.List -> Command.ChangeWidgetType.TYPE_LIST
                        }
                    )
                )
                isDismissed.value = true
            }
        }
    }

    class Factory(
        private val appCoroutineDispatchers: AppCoroutineDispatchers,
        private val payloadDispatcher: Dispatcher<Payload>,
        private val widgetDispatcher: Dispatcher<WidgetDispatchEvent>,
        private val updateWidget: UpdateWidget
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SelectWidgetTypeViewModel(
                appCoroutineDispatchers = appCoroutineDispatchers,
                payloadDispatcher = payloadDispatcher,
                widgetDispatcher = widgetDispatcher,
                updateWidget = updateWidget
            ) as T
        }
    }
}