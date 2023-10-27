package com.anytypeio.anytype.presentation.editor.template

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.TypeId
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
                    val typeKey = event.objType.uniqueKey
                    if (typeKey != null) {
                        val templates = getTemplates.run(
                            GetTemplates.Params(TypeId(event.objType.id))
                        )
                        if (templates.isNotEmpty()) {
                            SelectTemplateState.Available(
                                templates = templates.map { it.id },
                                typeId = event.objType.id,
                                typeKey = typeKey,
                                typeName = event.objType.name.orEmpty()
                            )
                        } else {
                            SelectTemplateState.Idle
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
                        typeId = state.typeId,
                        typeKey = state.typeKey,
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
        val typeId: Id,
        val typeKey: Id,
        val templates: List<Id>,
        val typeName: String
    ) : SelectTemplateState()

    /**
     * State where user accepted choosing a template for this object.
     */
    data class Accepted(
        val typeId: Id,
        val typeKey: Id,
        val templates: List<Id>,
    ) : SelectTemplateState()

    companion object {
        fun init(): SelectTemplateState = Idle
    }
}

sealed class SelectTemplateEvent {
    data class OnStart(
        val ctx: Id,
        val objType: ObjectWrapper.Type
    ) : SelectTemplateEvent()

    object OnSkipped : SelectTemplateEvent()
    object OnAccepted : SelectTemplateEvent()
}