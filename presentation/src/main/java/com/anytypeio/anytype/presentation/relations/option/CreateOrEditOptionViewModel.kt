package com.anytypeio.anytype.presentation.relations.option

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.relations.CreateRelationOption
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class CreateOrEditOptionViewModel(
    private val viewModelParams: ViewModelParams,
    private val values: ObjectValueProvider,
    private val createOption: CreateRelationOption,
    private val setObjectDetails: SetObjectDetails,
    private val dispatcher: Dispatcher<Payload>,
    private val spaceManager: SpaceManager,
    private val analytics: Analytics
) : BaseViewModel() {

    val command = MutableSharedFlow<Command>(replay = 0)
    val viewState: MutableStateFlow<OptionScreenViewState?>? = null

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