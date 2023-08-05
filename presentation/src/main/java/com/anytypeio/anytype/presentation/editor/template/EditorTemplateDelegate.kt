package com.anytypeio.anytype.presentation.editor.template

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.templates.ApplyTemplate
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.presentation.objects.isTemplatesAllowed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import timber.log.Timber

interface EditorTemplateDelegate {
    val templateDelegateState: Flow<SelectTemplateState>
    suspend fun onEvent(e: SelectTemplateEvent)
    fun clear()
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
                    if (event.objType == null) return@scan SelectTemplateState.Idle
                    val templates = getTemplates.run(GetTemplates.Params(event.objType.id))
                    if (templates.isNotEmpty()) {
                        SelectTemplateState.Available(
                            templates = templates,
                            type = event.objType.id,
                            typeName = event.objType.name.orEmpty(),
                            layout = event.objType.recommendedLayout ?: ObjectType.Layout.BASIC
                        )
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
                        templates = state.templates.map { it.id },
                    )
                else
                    SelectTemplateState.Idle
            }

            else -> SelectTemplateState.Idle
        }
    }.catch { e ->
        Timber.e(e, "Error while processing templates ")
    }

    override suspend fun onEvent(e: SelectTemplateEvent) {
        events.emit(e)
    }

    override fun clear() {}
}

class DefaultSetTemplateDelegate(
    private val getTemplates: GetTemplates,
    private val getDefaultPageType: GetDefaultPageType,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    dispatchers: AppCoroutineDispatchers
) : EditorTemplateDelegate {

    private val events = Channel<SelectTemplateEvent>(Channel.BUFFERED)

    private val _state =
        MutableStateFlow<SelectTemplateState>(SelectTemplateState.Idle)
    override val templateDelegateState: Flow<SelectTemplateState> get() = _state.asStateFlow()

    private val job = SupervisorJob()
    private val scope = CoroutineScope(dispatchers.io + job)

    init {
        scope.launch {
            events.consumeEach { event -> processEvent(event) }
        }
    }

    private suspend fun processEvent(
        event: SelectTemplateEvent
    ) {
        when (event) {
            is SelectTemplateEvent.OnStart -> processTemplates(event)
            is SelectTemplateEvent.OnSkipped -> setStateIdle()
            is SelectTemplateEvent.OnAccepted -> {}
        }
    }

    private suspend fun processTemplates(
        event: SelectTemplateEvent.OnStart
    ) {
        val objectType = getObjectType(event.objType?.id)
        if (objectType == null) {
            _state.value = SelectTemplateState.Error("Couldn't find ${event.objType?.name}")
            return
        }
        return if (objectType.isTemplatesAllowed()) {
            proceedWithGettingTemplates(objectType)
        } else {
            _state.value = SelectTemplateState.Error("Templates are not allowed for ${objectType.name}")
        }
    }

    private suspend fun getObjectType(type: Id?): ObjectWrapper.Type? {
        return if (type == null) {
            val defaultObjectType = getDefaultPageType.run(Unit).type ?: return null
            storeOfObjectTypes.get(defaultObjectType)
        } else {
            storeOfObjectTypes.get(type)
        }
    }

    private suspend fun proceedWithGettingTemplates(objType: ObjectWrapper.Type) {
        val params = GetTemplates.Params(objType.id)
        getTemplates.async(params).fold(
            onSuccess = { templates ->
                Timber.d("proceedWithGettingTemplates success: $templates")
                if (templates.isNotEmpty()) {
                    _state.value = SelectTemplateState.Available(
                        templates = templates,
                        type = objType.id,
                        typeName = objType.name.orEmpty(),
                        layout = objType.recommendedLayout ?: ObjectType.Layout.BASIC
                    )
                } else {
                    _state.value =
                        SelectTemplateState.Error("There is no templates for this type")
                }
            },
            onFailure = {
                Timber.e(it, "Error while getting templates")
                _state.value =
                    SelectTemplateState.Error("Error while getting templates")
            }
        )
    }

    private fun setStateIdle() {
        _state.value = SelectTemplateState.Idle
    }

    override suspend fun onEvent(e: SelectTemplateEvent) {
        events.send(e)
    }

    override fun clear() {
        job.cancel()
    }
}

sealed class SelectTemplateState {
    object Idle : SelectTemplateState()

    /**
     * State where templates are available for given object type.
     */
    data class Available(
        val type: Id,
        val typeName: String,
        val layout: ObjectType.Layout,
        val templates: List<ObjectWrapper.Basic>,
    ) : SelectTemplateState()

    /**
     * State where user accepted choosing a template for this object.
     */
    data class Accepted(
        val type: Id,
        val templates: List<Id>
    ) : SelectTemplateState()

    data class Error(val msg: String) : SelectTemplateState()

    companion object {
        fun init(): SelectTemplateState = Idle
    }
}

sealed class SelectTemplateEvent {
    data class OnStart(val ctx: Id, val objType: ObjectWrapper.Type? = null) : SelectTemplateEvent()
    object OnSkipped : SelectTemplateEvent()
    object OnAccepted : SelectTemplateEvent()
}