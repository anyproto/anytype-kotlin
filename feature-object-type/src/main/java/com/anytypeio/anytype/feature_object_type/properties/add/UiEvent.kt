package com.anytypeio.anytype.feature_object_type.properties.add

sealed class AddPropertyEvent {
    data class OnSearchQueryChanged(val query: String) : AddPropertyEvent()
    data class OnCreate(val item: UiAddPropertyItem.Create) : AddPropertyEvent()
    data class OnTypeClicked(val item: UiAddPropertyItem.Format) : AddPropertyEvent()
    data class OnExistingClicked(val item: UiAddPropertyItem.Default) : AddPropertyEvent()
}