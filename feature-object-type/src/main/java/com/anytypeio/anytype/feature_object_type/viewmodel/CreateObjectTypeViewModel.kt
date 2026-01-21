package com.anytypeio.anytype.feature_object_type.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary.libraryCreateType
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.types.CreateObjectType
import com.anytypeio.anytype.feature_object_type.ui.UiIconsPickerState
import com.anytypeio.anytype.feature_object_type.ui.create.UiTypeSetupTitleAndIconState
import com.anytypeio.anytype.feature_object_type.viewmodel.CreateTypeCommand.NavigateToObjectType
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateObjectTypeViewModel(
    private val vmParams: CreateTypeVmParams,
    private val createObjectType: CreateObjectType,
    private val analytics: Analytics,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
) : ViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    private val _uiState = MutableStateFlow<UiTypeSetupTitleAndIconState>(
        UiTypeSetupTitleAndIconState.Visible.CreateNewType(
            icon = ObjectIcon.TypeIcon.Default.DEFAULT
        )
    )
    val uiState = _uiState.asStateFlow()

    //icons picker screen
    val uiIconsPickerScreen = MutableStateFlow<UiIconsPickerState>(UiIconsPickerState.Hidden)

    val commands = MutableSharedFlow<CreateTypeCommand>(replay = 0)

    init {
        Timber.d("CreateObjectTypeViewModel initialized")
    }

    fun onDismiss() {
        viewModelScope.launch {
            commands.emit(CreateTypeCommand.Dismiss)
        }
    }

    fun onRemoveIcon() {
        val currentState =
            _uiState.value as? UiTypeSetupTitleAndIconState.Visible.CreateNewType ?: return
        _uiState.value = currentState.copy(icon = ObjectIcon.TypeIcon.Default.DEFAULT)
        uiIconsPickerScreen.value = UiIconsPickerState.Hidden
    }

    fun onNewIconPicked(iconName: String, color: CustomIconColor?) {
        val currentState =
            _uiState.value as? UiTypeSetupTitleAndIconState.Visible.CreateNewType ?: return
        val newIcon = ObjectIcon.TypeIcon.Default(
            rawValue = iconName,
            color = color ?: CustomIconColor.DEFAULT
        )
        _uiState.value = currentState.copy(icon = newIcon)
        uiIconsPickerScreen.value = UiIconsPickerState.Hidden
    }

    fun onIconClicked() {
        uiIconsPickerScreen.value = UiIconsPickerState.Visible
    }

    fun onDismissIconPicker() {
        uiIconsPickerScreen.value = UiIconsPickerState.Hidden
    }

    fun onButtonClicked(title: String, plurals: String) {
        createNewType(title = title, plurals = plurals)
    }

    private fun createNewType(title: String, plurals: String) {
        val state = _uiState.value as? UiTypeSetupTitleAndIconState.Visible.CreateNewType ?: return
        val icon = state.icon
        Timber.d("Creating new type with title: $title, plurals: $plurals, icon: $icon")
        viewModelScope.launch {
            createObjectType.execute(
                CreateObjectType.Params(
                    space = vmParams.spaceId,
                    name = title,
                    pluralName = plurals,
                    iconName = icon.rawValue,
                    iconColor = icon.color.iconOption.toDouble()
                )
            ).fold(
                onSuccess = { objTypeId ->
                    val spaceParams = provideParams(vmParams.spaceId)
                    viewModelScope.sendEvent(
                        analytics = analytics,
                        eventName = libraryCreateType,
                        props = Props(
                            mapOf(
                                EventsPropertiesKey.permissions to spaceParams.permission,
                                EventsPropertiesKey.spaceType to spaceParams.spaceType,
                                EventsPropertiesKey.uxType to spaceParams.spaceUxType,
                                EventsPropertiesKey.spaceId to spaceParams.spaceId
                            )
                        )
                    )

                    commands.emit(
                        NavigateToObjectType(
                            id = objTypeId,
                            space = vmParams.spaceId
                        )
                    )
                },
                onFailure = {
                    Timber.e(it, "Error while creating type")
                }
            )
        }

    }
}

data class CreateTypeVmParams(
    val spaceId: Id
)

sealed class CreateTypeCommand {
    data object Dismiss : CreateTypeCommand()
    data class NavigateToObjectType(val id: Id, val space: Id) : CreateTypeCommand()
}

class CreateObjectTypeVMFactory @Inject constructor(
    private val vmParams: CreateTypeVmParams,
    private val createObjectType: CreateObjectType,
    private val analytics: Analytics,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = CreateObjectTypeViewModel(
        vmParams = vmParams,
        createObjectType = createObjectType,
        analytics = analytics,
        analyticSpaceHelperDelegate = analyticSpaceHelperDelegate
    ) as T
}