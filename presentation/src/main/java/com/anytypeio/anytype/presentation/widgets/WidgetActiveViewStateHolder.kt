package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.tools.toPrettyString
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import timber.log.Timber

interface WidgetActiveViewStateHolder {

    fun init(map: WidgetToActiveView)
    fun onChangeCurrentWidgetView(widget: Id, view: Id)
    fun observeCurrentWidgetView(widget: Id): Flow<Id?>

    class Impl @Inject constructor() : WidgetActiveViewStateHolder {
        private val widgetToActiveView = MutableStateFlow<WidgetToActiveView>(mapOf())

        override fun init(map: WidgetToActiveView) {
            Timber.d("Initializing active view: ${map.toPrettyString()}")
            widgetToActiveView.value = map
        }

        override fun onChangeCurrentWidgetView(widget: Id, view: Id) {
            widgetToActiveView.value = widgetToActiveView.value + mapOf(widget to view)
        }

        override fun observeCurrentWidgetView(widget: Id) = widgetToActiveView.mapLatest { map ->
            map[widget]
        }
    }
}

typealias WidgetToActiveView = Map<Id, Id>
typealias WidgetActiveViewId = Id