package com.anytypeio.anytype.presentation.page.layout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Layout
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.layout.SetObjectLayout
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.page.Editor
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectLayoutViewModel(
    private val setObjectLayout: SetObjectLayout,
    private val dispatcher: Dispatcher<Payload>,
    private val storage: Editor.Storage
): BaseViewModel() {

    private val jobs = mutableListOf<Job>()

    val layout = MutableStateFlow(Layout.BASIC)

    fun onStart(ctx: Id) {
        jobs += viewModelScope.launch {
            storage.details.stream().collect { details ->
                val code = details.details[ctx]?.layout?.toInt()
                layout.value = Layout.values().find { it.code == code } ?: Layout.BASIC
            }
        }
    }

    fun onStop() {
        jobs.apply {
            forEach { it.cancel() }
            clear()
        }
    }

    fun onLayouClicked(
        ctx: Id,
        layout: Layout
    ) {
        viewModelScope.launch {
            setObjectLayout(
                SetObjectLayout.Params(
                    ctx = ctx,
                    layout = layout
                )
            ).process(
                failure = { Timber.e(it, ERROR_MESSAGE).also { _toasts.emit(ERROR_MESSAGE) } },
                success = { dispatcher.send(it) }
            )
        }
    }

    class Factory(
        private val setObjectLayout: SetObjectLayout,
        private val dispatcher: Dispatcher<Payload>,
        private val storage: Editor.Storage
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ObjectLayoutViewModel(
                dispatcher = dispatcher,
                setObjectLayout = setObjectLayout,
                storage = storage
            ) as T
        }
    }

    companion object {
        const val ERROR_MESSAGE = "Error while updating object's layout"
    }
}