package com.anytypeio.anytype.presentation.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ext.asMap
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.templates.OpenTemplate
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.render.BlockViewRenderer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class TemplateViewModel(
    private val openTemplate: OpenTemplate,
    private val renderer: BlockViewRenderer,
    private val dispatchers: AppCoroutineDispatchers
) : BaseViewModel(), BlockViewRenderer by renderer {

    val state = MutableStateFlow<List<BlockView>>(emptyList())

    fun onStart(ctx: Id) {
        viewModelScope.launch {
            state.value = openTemplate.asFlow(OpenTemplate.Params(ctx))
                .map { result ->
                    when(result) {
                        is Result.Failure -> {
                            emptyList()
                        }
                        is Result.Success -> {
                            val event = result.data.events
                                .filterIsInstance<Event.Command.ShowObject>()
                                .first()
                            val root = event.blocks.first { it.id == ctx }
                            event.blocks.asMap().render(
                                mode = Editor.Mode.Read,
                                root = root,
                                focus = com.anytypeio.anytype.domain.editor.Editor.Focus.empty(),
                                anchor = ctx,
                                indent = EditorViewModel.INITIAL_INDENT,
                                details = event.details,
                                relationLinks = event.relationLinks,
                                restrictions = event.objectRestrictions,
                                selection = emptySet()
                            )
                        }
                    }
                }
                .catch { Timber.e(it, "Error while opening template") }
                .flowOn(dispatchers.io)
                .single()
        }
    }


    class Factory @Inject constructor(
        private val openTemplate: OpenTemplate,
        private val renderer: BlockViewRenderer,
        private val dispatchers: AppCoroutineDispatchers
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TemplateViewModel(
                openTemplate = openTemplate,
                renderer = renderer,
                dispatchers = dispatchers
            ) as T
        }
    }
}