package com.anytypeio.anytype.presentation.editor.layout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.layout.GetSupportedObjectLayouts
import com.anytypeio.anytype.domain.layout.SetObjectLayout
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.mapper.toObjectLayout
import com.anytypeio.anytype.presentation.mapper.toView
import com.anytypeio.anytype.presentation.objects.ObjectLayoutView
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectLayoutViewModel(
    private val setObjectLayout: SetObjectLayout,
    private val getSupportedObjectLayouts: GetSupportedObjectLayouts,
    private val dispatcher: Dispatcher<Payload>,
    private val storage: Editor.Storage
): BaseViewModel() {

    private var supportedObjectLayouts = MutableStateFlow<List<ObjectLayoutView>>(emptyList())
    private var selectedLayout = MutableStateFlow<Int?>(null)

    val views = MutableStateFlow<List<ObjectLayoutView>>(emptyList())

    fun onStart(ctx: Id) {
        viewModelScope.launch {
            combine(supportedObjectLayouts, selectedLayout) { layouts, id ->
                layouts.map {
                    if (it.id == id) {
                        it.copy(isSelected = true)
                    } else {
                        it.copy(isSelected = false)
                    }
                }
            }.collectLatest {
                views.value = it
            }
        }
        proceedWithSupportLayouts(ctx)
    }

    private fun proceedWithSupportLayouts(ctx: Id) {
        viewModelScope.launch {
            getSupportedObjectLayouts.invoke(GetSupportedObjectLayouts.Params(ctx)).proceed(
                failure = { Timber.e(it, "Error while getting support layouts") },
                success = { layouts ->
                    supportedObjectLayouts.value = layouts.toView()
                    proceedWithObjectLayout(ctx)
                }
            )
        }
    }

    private fun proceedWithObjectLayout(ctx: Id) {
        viewModelScope.launch {
            storage.details.stream().collect { details ->
                val code = details.details[ctx]?.layout?.toInt()
                selectedLayout.value = code ?: ObjectType.Layout.BASIC.code
            }
        }
    }

    fun onLayoutClicked(
        ctx: Id,
        layoutView: ObjectLayoutView
    ) {
        val params = SetObjectLayout.Params(
            ctx = ctx,
            layout = layoutView.toObjectLayout()
        )
        viewModelScope.launch {
            setObjectLayout(params).process(
                failure = {
                    Timber.e(it, ERROR_MESSAGE)
                    _toasts.emit(ERROR_MESSAGE)
                },
                success = { dispatcher.send(it) }
            )
        }
    }

    class Factory(
        private val setObjectLayout: SetObjectLayout,
        private val getSupportedObjectLayouts: GetSupportedObjectLayouts,
        private val dispatcher: Dispatcher<Payload>,
        private val storage: Editor.Storage
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ObjectLayoutViewModel(
                dispatcher = dispatcher,
                setObjectLayout = setObjectLayout,
                getSupportedObjectLayouts = getSupportedObjectLayouts,
                storage = storage
            ) as T
        }
    }

    companion object {
        const val ERROR_MESSAGE = "Error while updating object's layout"
    }
}