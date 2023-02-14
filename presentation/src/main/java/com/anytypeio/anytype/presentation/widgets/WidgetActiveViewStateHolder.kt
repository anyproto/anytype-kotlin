package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest

interface WidgetActiveViewStateHolder {

    fun onChangeCurrentWidgetView(widget: Id, view: Id)
    fun observeCurrentWidgetView(widget: Id): Flow<Id?>

    class Impl @Inject constructor() : WidgetActiveViewStateHolder {
        private val widgetToActiveView = MutableStateFlow<WidgetToActiveView>(mapOf())

        override fun onChangeCurrentWidgetView(widget: Id, view: Id) {
            widgetToActiveView.value = widgetToActiveView.value + mapOf(widget to view)
        }

        override fun observeCurrentWidgetView(widget: Id) = widgetToActiveView.mapLatest { map ->
            map[widget]
        }
    }
}

typealias WidgetToActiveView = Map<Id, Id>