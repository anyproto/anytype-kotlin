package com.anytypeio.anytype.presentation.widgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.WidgetLayout
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.widgets.UpdateWidget
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectWidgetTypeViewModel(
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val dispatcher: Dispatcher<Payload>,
    private val updateWidget: UpdateWidget
) : BaseViewModel() {

    val views = MutableStateFlow(
        listOf(
            WidgetTypeView.Tree(isSelected = false),
            WidgetTypeView.Link(isSelected = false)
        )
    )

    val isDismissed = MutableStateFlow(false)

    fun onStart(currentType: Int) {
        views.value = views.value.map { view -> view.setIsSelected(currentType) }
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
                            is WidgetTypeView.List -> TODO()
                        }
                    )
                ).flowOn(appCoroutineDispatchers.io).collect { result ->
                    result.fold(
                        onFailure = {
                            Timber.e(it, "Error while updating widget type")
                        },
                        onSuccess = {
                            dispatcher.send(it).also {
                                isDismissed.value = true
                            }
                        }
                    )
                }
            }
        }
    }

    class Factory(
        private val appCoroutineDispatchers: AppCoroutineDispatchers,
        private val dispatcher: Dispatcher<Payload>,
        private val updateWidget: UpdateWidget
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SelectWidgetTypeViewModel(
                appCoroutineDispatchers = appCoroutineDispatchers,
                dispatcher = dispatcher,
                updateWidget = updateWidget
            ) as T
        }
    }
}