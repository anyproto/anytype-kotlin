package com.anytypeio.anytype.feature_properties.add

sealed class UiEditTypePropertiesEvent {
    data class OnSearchQueryChanged(val query: String) : UiEditTypePropertiesEvent()
    data class OnCreate(val item: UiEditTypePropertiesItem.Create) : UiEditTypePropertiesEvent()
    data class OnTypeClicked(val item: UiEditTypePropertiesItem.Format) : UiEditTypePropertiesEvent()
    data class OnExistingClicked(val item: UiEditTypePropertiesItem.Default) : UiEditTypePropertiesEvent()
    data object OnCreateNewButtonClicked : UiEditTypePropertiesEvent()
    data object OnSaveButtonClicked : UiEditTypePropertiesEvent()
    data object OnEditPropertyScreenDismissed : UiEditTypePropertiesEvent()
    data class OnPropertyNameUpdate(val name: String) : UiEditTypePropertiesEvent()
}