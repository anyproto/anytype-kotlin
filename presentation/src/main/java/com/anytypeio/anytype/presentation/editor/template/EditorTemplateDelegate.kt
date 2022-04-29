package com.anytypeio.anytype.presentation.editor.template

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.templates.ApplyTemplate
import com.anytypeio.anytype.domain.templates.GetTemplates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.scan
import timber.log.Timber

interface EditorTemplateDelegate {
    val templateDelegateState: Flow<SelectTemplateState>
    suspend fun onEvent(e: SelectTemplateEvent)
}

class DefaultEditorTemplateDelegate(
    private val getTemplates: GetTemplates,
    private val applyTemplate: ApplyTemplate
) : EditorTemplateDelegate {

    private val events = MutableSharedFlow<SelectTemplateEvent>(replay = 0)

    override val templateDelegateState = events.scan(SelectTemplateState.init()) { state, event ->
        when (event) {
            is SelectTemplateEvent.OnStart -> {
                try {
                    val templates = getTemplates.run(GetTemplates.Params(event.type))
                    if (templates.isNotEmpty()) {
                        if (templates.size == 1) {
                            // No need to choose template if there is only one available template.
                            applyTemplate.run(
                                ApplyTemplate.Params(
                                    ctx = event.ctx,
                                    template = templates.first().id
                                )
                            )
                            SelectTemplateState.Idle
                        } else {
                            SelectTemplateState.Available(
                                templates = templates.map { it.id },
                                type = event.type
                            )
                        }
                    } else {
                        SelectTemplateState.Idle
                    }
                } catch (e: Exception) {
                    SelectTemplateState.Idle
                }
            }
            is SelectTemplateEvent.OnSkipped -> SelectTemplateState.Idle
            is SelectTemplateEvent.OnAccepted -> {
                if (state is SelectTemplateState.Available)
                    SelectTemplateState.Accepted(
                        type = state.type,
                        templates = state.templates
                    )
                else
                    SelectTemplateState.Idle
            }
        }
    }.catch { e ->
        Timber.e(e, "Error while processing templates ")
    }

    override suspend fun onEvent(e: SelectTemplateEvent) {
        events.emit(e)
    }
}

sealed class SelectTemplateState {
    object Idle : SelectTemplateState()

    /**
     * State where templates are available for given object type.
     */
    data class Available(
        val type: Id,
        val templates: List<Id>
    ) : SelectTemplateState()

    /**
     * State where user accepted choosing a template for this object.
     */
    data class Accepted(
        val type: Id,
        val templates: List<Id>
    ) : SelectTemplateState()

    companion object {
        fun init(): SelectTemplateState = Idle
    }
}

sealed class SelectTemplateEvent {
    data class OnStart(val ctx: Id, val type: Id) : SelectTemplateEvent()
    object OnSkipped : SelectTemplateEvent()
    object OnAccepted : SelectTemplateEvent()
}