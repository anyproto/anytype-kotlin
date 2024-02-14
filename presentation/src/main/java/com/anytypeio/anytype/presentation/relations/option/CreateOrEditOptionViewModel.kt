package com.anytypeio.anytype.presentation.relations.option

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.StoreOfRelations
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

class CreateOrEditOptionViewModel(
    private val viewModelParams: ViewModelParams,
    private val values: ObjectValueProvider,
    private val createOption: CreateRelationOption,
    private val setObjectDetails: SetObjectDetails,
    private val dispatcher: Dispatcher<Payload>,
    private val spaceManager: SpaceManager,
    private val analytics: Analytics,
    private val storeOfRelations: StoreOfRelations
) : BaseViewModel() {

    val command = MutableSharedFlow<Command>(replay = 0)
    val viewState: MutableStateFlow<CreateOrEditOptionScreenViewState> =
        MutableStateFlow(initialViewState())

    private fun initialViewState(): CreateOrEditOptionScreenViewState {
        val optionId = viewModelParams.optionId
        val color = getOptionColor()
        return if (optionId != null) {
            Timber.d("Editing option with id: $optionId")
            CreateOrEditOptionScreenViewState.Edit(
                optionId = optionId,
                text = viewModelParams.name.orEmpty(),
                color = color
            )

        } else {
            Timber.d("Creating new option")
            CreateOrEditOptionScreenViewState.Create(
                text = viewModelParams.name.orEmpty(),
                color = color
            )
        }
    }

    fun updateName(name: String) {
        viewState.value = when (val state = viewState.value) {
            is CreateOrEditOptionScreenViewState.Create -> state.copy(text = name)
            is CreateOrEditOptionScreenViewState.Edit -> state.copy(text = name)
        }
    }

    fun updateColor(color: ThemeColor) {
        viewState.value = when (val state = viewState.value) {
            is CreateOrEditOptionScreenViewState.Create -> state.copy(color = color)
            is CreateOrEditOptionScreenViewState.Edit -> state.copy(color = color)
        }
    }

    fun onButtonClick() {
        when (viewState.value) {
            is CreateOrEditOptionScreenViewState.Create -> proceedWithCreatingOption()
            is CreateOrEditOptionScreenViewState.Edit -> proceedWithUpdatingOption()
        }
    }

    private fun proceedWithCreatingOption() {
        viewModelScope.launch {
            val params = CreateRelationOption.Params(
                space = spaceManager.get(),
                relation = viewModelParams.relationKey,
                name = viewState.value.text,
                color = viewState.value.color.code
            )
            if (params.name.isNotEmpty()) {
                createOption.invoke(params).proceed(
                    success = { option ->
                        proceedWithAddingTagOrStatusToObject(
                            ctx = viewModelParams.ctx,
                            objectId = viewModelParams.objectId,
                            relationKey = viewModelParams.relationKey,
                            option = option
                        )
                    },
                    failure = { Timber.e(it, "Error while creating option") }
                )
            }
        }
    }

    private fun proceedWithUpdatingOption() {
        val optionId = viewModelParams.optionId ?: return
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

    private suspend fun proceedWithAddingTagOrStatusToObject(
        ctx: Id,
        objectId: Id,
        relationKey: Key,
        option: ObjectWrapper.Option
    ) {
        Timber.d("Adding option to object with id: $objectId")
        val relation = storeOfRelations.getByKey(relationKey) ?: return
        val obj = values.get(target = objectId, ctx = ctx)
        val result = updatedValues(option.id, relation, obj)
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

    private fun updatedValues(
        newOptionId: Id,
        relation: ObjectWrapper.Relation,
        obj: Struct
    ): List<Id> {
        return when (relation.format) {
            Relation.Format.STATUS -> {
                listOf(newOptionId)
            }
            Relation.Format.TAG -> {
                buildList {
                    val value = obj[relation.key]
                    if (value is List<*>) {
                        addAll(value.typeOf())
                    } else if (value is Id) {
                        add(value)
                    }
                    add(newOptionId)
                }
            }
            else -> throw IllegalArgumentException("Unsupported relation format: ${relation.format}")
        }
    }

    private fun getOptionColor(): ThemeColor {
        val color = viewModelParams.color
        return if (color != null) {
            ThemeColor.fromCode(color)
        } else {
            ThemeColor.values().filter { it != ThemeColor.DEFAULT }.random()
        }
    }

    data class ViewModelParams(
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

sealed class CreateOrEditOptionScreenViewState {
    abstract val text: String
    abstract val color: ThemeColor

    data class Edit(
        val optionId: Id,
        override val text: String,
        override val color: ThemeColor
    ) : CreateOrEditOptionScreenViewState()

    data class Create(
        override val text: String,
        override val color: ThemeColor
    ) : CreateOrEditOptionScreenViewState()
}