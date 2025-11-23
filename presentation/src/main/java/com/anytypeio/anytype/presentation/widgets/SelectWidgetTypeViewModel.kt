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
import com.anytypeio.anytype.core_models.widgets.BundledWidgetSourceIds
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

    val views = MutableStateFlow<List<WidgetTypeView>>(
        listOf(WidgetTypeView.Link(isSelected = false))
    )

    val isDismissed = MutableStateFlow(false)

    fun onStartForExistingWidget(
        currentType: Int,
        sourceLayout: Int,
        source: Id
    ) {
        Timber.d("onStartForExistingWidget,  currentType:[$currentType], sourceLayout:[$sourceLayout], source:[$source]")

        views.value = when {
            // Check if the source is a bundled widget
            BundledWidgetSourceIds.ids.contains(source) -> {
                // Determine if the source requires additional widget types
                val supportTreeLayout = source in setOf(
                    BundledWidgetSourceIds.FAVORITE,
                    BundledWidgetSourceIds.RECENT,
                    BundledWidgetSourceIds.RECENT_LOCAL
                )

                // Base widget types for bundled sources
                val defaultWidgetTypes = listOf(
                    WidgetTypeView.CompactList(),
                    WidgetTypeView.List()
                )

                // Add Tree widget type if it's an extended source
                val widgetList = if (supportTreeLayout) {
                    defaultWidgetTypes + WidgetTypeView.Tree()
                } else {
                    defaultWidgetTypes
                }

                // Set the selected state
                widgetList.map { it.setIsSelected(currentType) }
            }

            // Handle non-bundled widget sources
            else -> {
                // Find the corresponding ObjectType.Layout based on sourceLayout
                val objectLayout = ObjectType.Layout.entries.find { it.code == sourceLayout }

                when {
                    // If the layout is a data view, provide View and Link widgets
                    objectLayout?.isDataView() == true -> listOf(
                        WidgetTypeView.View(),
                        WidgetTypeView.CompactList(),
                        WidgetTypeView.List(),
                        WidgetTypeView.Link()
                    )

                    // If the layout is PARTICIPANT, provide only the Link widget
                    objectLayout == ObjectType.Layout.PARTICIPANT -> listOf(
                        WidgetTypeView.Link()
                    )

                    // If the layout is DATE, provide only the Link widget
                    objectLayout == ObjectType.Layout.DATE -> listOf(
                        WidgetTypeView.Link()
                    )

                    objectLayout == ObjectType.Layout.OBJECT_TYPE -> {
                        listOf(
                            WidgetTypeView.View(),
                            WidgetTypeView.CompactList(),
                            WidgetTypeView.List(),
                            WidgetTypeView.Link()
                        )
                    }

                    objectLayout in listOf(
                        ObjectType.Layout.BASIC,
                        ObjectType.Layout.NOTE,
                        ObjectType.Layout.TODO,
                        ObjectType.Layout.PROFILE
                    ) -> {
                        listOf(
                            WidgetTypeView.Tree(),
                            WidgetTypeView.Link()
                        )
                    }

                    // For other layouts, update existing views with the selected state
                    else -> views.value
                }.map { it.setIsSelected(currentType) }
            }
        }
    }

    fun onStartForNewWidget(layout: Int, source: Id) {
        Timber.d("onStartForNewWidget, layout:[$layout], source:[$source]")

        views.value = when {
            // Check if the source is a bundled widget
            BundledWidgetSourceIds.ids.contains(source) -> {
                // Determine if the source requires additional widget types
                val supportTreeLayout = source in setOf(
                    BundledWidgetSourceIds.FAVORITE,
                    BundledWidgetSourceIds.RECENT,
                    BundledWidgetSourceIds.RECENT_LOCAL
                )

                // Base widget types for bundled sources
                val baseWidgets = listOf(
                    WidgetTypeView.CompactList(isSelected = false),
                    WidgetTypeView.List(isSelected = false)
                )

                // Add Tree widget type if it's an extended source
                val widgetList = if (supportTreeLayout) {
                    baseWidgets + WidgetTypeView.Tree(isSelected = false)
                } else {
                    baseWidgets
                }

                widgetList
            }

            // Handle non-bundled widget sources
            else -> {
                // Find the corresponding ObjectType.Layout based on layout
                val objectLayout = ObjectType.Layout.entries.find { it.code == layout }

                when {
                    // If the layout is a data view, provide View and Link widgets
                    objectLayout?.isDataView() == true -> listOf(
                        WidgetTypeView.View(isSelected = false),
                        WidgetTypeView.CompactList(),
                        WidgetTypeView.List(),
                        WidgetTypeView.Link(isSelected = false)
                    )

                    // If the layout is PARTICIPANT or DATE, provide only the Link widget (selected)
                    objectLayout == ObjectType.Layout.PARTICIPANT ||
                            objectLayout == ObjectType.Layout.DATE -> listOf(
                        WidgetTypeView.Link(isSelected = true)
                    )

                    objectLayout == ObjectType.Layout.OBJECT_TYPE -> {
                        listOf(
                            WidgetTypeView.View(isSelected = false),
                            WidgetTypeView.CompactList(),
                            WidgetTypeView.List(),
                            WidgetTypeView.Link()
                        )
                    }

                    // For other layouts, provide Tree and Link widgets (not selected)
                    else -> listOf(
                        WidgetTypeView.Tree(isSelected = false),
                        WidgetTypeView.Link(isSelected = false)
                    )
                }
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
                    is WidgetTypeView.View -> WidgetLayout.VIEW
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
        } else {
            isDismissed.value = true
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
        Timber.d("onWidgetTypeClicked, source:[$source], target:[$target], view:[$view]")
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
                            is WidgetTypeView.View -> Command.ChangeWidgetType.TYPE_VIEW
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