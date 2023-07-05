package com.anytypeio.anytype.presentation.widgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.WidgetLayout
import com.anytypeio.anytype.core_models.isDataView
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.widgets.UpdateWidget
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendChangeWidgetLayoutEvent
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
    private val updateWidget: UpdateWidget,
    private val analytics: Analytics
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
            if (source == BundledWidgetSourceIds.FAVORITE || source == BundledWidgetSourceIds.RECENT) {
                views.value = listOf(
                    WidgetTypeView.CompactList().setIsSelected(currentType),
                    WidgetTypeView.List().setIsSelected(currentType),
                    WidgetTypeView.Tree().setIsSelected(currentType),
                )
            } else {
                views.value = listOf(
                    WidgetTypeView.CompactList().setIsSelected(currentType),
                    WidgetTypeView.List().setIsSelected(currentType)
                )
            }
        } else {
            val objectLayout = ObjectType.Layout.values().find { it.code == sourceLayout }
            if (objectLayout.isDataView()) {
                views.value = listOf(
                    WidgetTypeView.CompactList().setIsSelected(currentType),
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
            if (source == BundledWidgetSourceIds.FAVORITE || source == BundledWidgetSourceIds.RECENT) {
                views.value = listOf(
                    WidgetTypeView.CompactList(isSelected = false),
                    WidgetTypeView.List(isSelected = false),
                    WidgetTypeView.Tree(isSelected = false),
                )
            } else {
                views.value = listOf(
                    WidgetTypeView.CompactList(isSelected = false),
                    WidgetTypeView.List(isSelected = false),
                )
            }
        } else {
            val objectLayout = ObjectType.Layout.values().find { it.code == layout }
            if (objectLayout.isDataView()) {
                views.value = listOf(
                    WidgetTypeView.CompactList(isSelected = false),
                    WidgetTypeView.List(isSelected = false),
                    WidgetTypeView.Link(isSelected = false)
                )
            } else {
                views.value = listOf(
                    WidgetTypeView.Tree(isSelected = false),
                    WidgetTypeView.Link(isSelected = false)
                )
            }
        }
    }

    /**
     * Flow for an existing widget
     */
    fun onWidgetTypeClicked(
        ctx: Id,
        widget: Id,
        source: Id,
        view: WidgetTypeView,
        isInEditMode: Boolean
    ) {
        if (!view.isSelected) {
            viewModelScope.launch {
                val newLayout = when (view) {
                    is WidgetTypeView.Link -> WidgetLayout.LINK
                    is WidgetTypeView.Tree -> WidgetLayout.TREE
                    is WidgetTypeView.List -> WidgetLayout.LIST
                    is WidgetTypeView.CompactList -> WidgetLayout.COMPACT_LIST
                }
                updateWidget(
                    UpdateWidget.Params(
                        ctx = ctx,
                        widget = widget,
                        source = source,
                        type = newLayout
                    )
                ).flowOn(appCoroutineDispatchers.io).collect { result ->
                    result.fold(
                        onFailure = {
                            Timber.e(it, "Error while updating widget type")
                        },
                        onSuccess = {
                            sendChangeWidgetLayoutEvent(
                                analytics = analytics,
                                layout = newLayout,
                                isInEditMode = isInEditMode
                            )
                            payloadDispatcher.send(it).also {
                                isDismissed.value = true
                            }
                        }
                    )
                }
            }
        }
    }

    /**
     * Flow for a new widget
     */
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
                            is WidgetTypeView.CompactList -> Command.ChangeWidgetType.TYPE_COMPACT_LIST
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
        private val updateWidget: UpdateWidget,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SelectWidgetTypeViewModel(
                appCoroutineDispatchers = appCoroutineDispatchers,
                payloadDispatcher = payloadDispatcher,
                widgetDispatcher = widgetDispatcher,
                updateWidget = updateWidget,
                analytics = analytics
            ) as T
        }
    }
}