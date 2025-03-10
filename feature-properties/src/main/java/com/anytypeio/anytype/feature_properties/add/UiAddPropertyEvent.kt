package com.anytypeio.anytype.feature_properties.add

sealed class AddPropertyEvent {
    data class OnSearchQueryChanged(val query: String) : AddPropertyEvent()
    data class OnCreate(val item: UiAddPropertyItem.Create) : AddPropertyEvent()
    data class OnTypeClicked(val item: UiAddPropertyItem.Format) : AddPropertyEvent()
    data class OnExistingClicked(val item: UiAddPropertyItem.Default) : AddPropertyEvent()
    data object OnCreateNewButtonClicked : AddPropertyEvent()
    data object OnSaveButtonClicked : AddPropertyEvent()
    data object OnEditPropertyScreenDismissed : AddPropertyEvent()
    data class OnPropertyNameUpdate(val name: String) : AddPropertyEvent()
}