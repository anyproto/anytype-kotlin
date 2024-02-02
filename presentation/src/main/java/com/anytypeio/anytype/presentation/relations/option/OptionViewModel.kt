package com.anytypeio.anytype.presentation.relations.option

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.relations.CreateRelationOption
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationValueEvent
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class OptionViewModel(
    private val vmParams: Params,
    private val values: ObjectValueProvider,
    private val createOption: CreateRelationOption,
    private val setObjectDetails: SetObjectDetails,
    private val dispatcher: Dispatcher<Payload>,
    private val spaceManager: SpaceManager,
    private val analytics: Analytics,
) : BaseViewModel() {

    val command = MutableSharedFlow<Command>(replay = 0)
    val viewState: MutableStateFlow<OptionScreenViewState> =
        if (vmParams.optionId == null) {
            val color = if (vmParams.color != null) {
                ThemeColor.fromCode(vmParams.color)
            } else {
                ThemeColor.values().drop(1).random()
            }
            MutableStateFlow(
                OptionScreenViewState.Create(
                    text = vmParams.name.orEmpty(),
                    color = color
                )
            )
        } else {
            val color = if (vmParams.color != null) {
                ThemeColor.fromCode(vmParams.color)
            } else {
                ThemeColor.values().drop(1).random()
            }
            MutableStateFlow(
                OptionScreenViewState.Edit(
                    optionId = vmParams.optionId,
                    text = vmParams.name.orEmpty(),
                    color = color
                )
            )
        }

    fun updateName(name: String) {
        viewState.value = when (val state = viewState.value) {
            is OptionScreenViewState.Create -> state.copy(text = name)
            is OptionScreenViewState.Edit -> state.copy(text = name)
        }
    }

    fun updateColor(color: ThemeColor) {
        viewState.value = when (val state = viewState.value) {
            is OptionScreenViewState.Create -> state.copy(color = color)
            is OptionScreenViewState.Edit -> state.copy(color = color)
        }
    }

    fun onButtonClick() {
        when (viewState.value) {
            is OptionScreenViewState.Create -> createOption()
            is OptionScreenViewState.Edit -> updateOption()
        }
    }

    private fun createOption() {
        viewModelScope.launch {
            val params = CreateRelationOption.Params(
                space = spaceManager.get(),
                relation = vmParams.relationKey,
                name = viewState.value.text,
                color = viewState.value.color.code
            )
            if (params.name.isEmpty()) {
                return@launch
            }
            createOption.invoke(params).proceed(
                success = { option ->
                    proceedWithAddingTagToObject(
                        ctx = vmParams.ctx,
                        objectId = vmParams.objectId,
                        relationKey = vmParams.relationKey,
                        option = option
                    )
                },
                failure = { Timber.e(it, "Error while creating option") }
            )
        }
    }

    private fun updateOption() {
        val optionId = vmParams.optionId ?: return
        viewModelScope.launch {
            val params = SetObjectDetails.Params(
                ctx = optionId,
                details = mapOf(
                    Relations.NAME to viewState.value.text,
                    Relations.RELATION_OPTION_COLOR to viewState.value.color.code
                )
            )
            setObjectDetails.execute(params).fold(
                onFailure = { Timber.e(it, "Error while updating option") },
                onSuccess = {
                    dispatcher.send(it)
                    viewModelScope.sendAnalyticsRelationValueEvent(analytics)
                    command.emit(Command.Dismiss)
                }
            )
        }
    }

    private suspend fun proceedWithAddingTagToObject(
        ctx: Id,
        objectId: Id,
        relationKey: Key,
        option: ObjectWrapper.Option
    ) {
        Timber.d("Adding option to object with id: $objectId")
        val obj = values.get(target = objectId, ctx = ctx)
        val result = mutableListOf<Id>()
        val value = obj[relationKey]
        if (value is List<*>) {
            result.addAll(value.typeOf())
        } else if (value is Id) {
            result.add(value)
        }
        result.add(option.id)
        val params = SetObjectDetails.Params(
            ctx = objectId,
            details = mapOf(relationKey to result)
        )
        setObjectDetails.execute(params).fold(
            onFailure = { Timber.e(it, "Error while adding tag to object") },
            onSuccess = {
                dispatcher.send(it)
                viewModelScope.sendAnalyticsRelationValueEvent(analytics)
                command.emit(Command.Dismiss)
            }
        )
    }

    data class Params(
        val ctx: Id,
        val relationKey: Key,
        val objectId: Id,
        val optionId: Id?,
        val name: String?,
        val color: String?
    )

    sealed class Command {
        object Dismiss : Command()
    }
}

sealed class OptionScreenViewState {
    abstract val text: String
    abstract val color: ThemeColor

    data class Edit(
        val optionId: Id,
        override val text: String,
        override val color: ThemeColor
    ) : OptionScreenViewState()

    data class Create(
        override val text: String,
        override val color: ThemeColor
    ) : OptionScreenViewState()
}