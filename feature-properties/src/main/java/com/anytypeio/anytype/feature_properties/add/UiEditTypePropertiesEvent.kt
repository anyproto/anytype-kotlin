package com.anytypeio.anytype.feature_properties.add

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.feature_properties.edit.UiPropertyLimitTypeItem

sealed class UiEditTypePropertiesEvent {
    data class OnSearchQueryChanged(val query: String) : UiEditTypePropertiesEvent()
    data class OnCreate(val item: UiEditTypePropertiesItem.Create) : UiEditTypePropertiesEvent()
    data class OnTypeClicked(val item: UiEditTypePropertiesItem.Format) : UiEditTypePropertiesEvent()
    data class OnExistingClicked(val item: UiEditTypePropertiesItem.Default) : UiEditTypePropertiesEvent()
    data object OnCreateNewButtonClicked : UiEditTypePropertiesEvent()
    data object OnSaveButtonClicked : UiEditTypePropertiesEvent()
    data object OnEditPropertyScreenDismissed : UiEditTypePropertiesEvent()
    data class OnPropertyNameUpdate(val name: String) : UiEditTypePropertiesEvent()

    data object OnLimitTypesClick : UiEditTypePropertiesEvent()
    data object OnLimitTypesDismiss : UiEditTypePropertiesEvent()
    data class OnLimitTypesDoneClick(val items: List<Id>) : UiEditTypePropertiesEvent()

    data object OnPropertyFormatClick : UiEditTypePropertiesEvent()
    data object OnPropertyFormatsListDismiss : UiEditTypePropertiesEvent()
    data class OnPropertyFormatSelected(val format: UiEditTypePropertiesItem.Format) : UiEditTypePropertiesEvent()
}