package com.anytypeio.anytype.feature_properties.space.ui

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.feature_properties.add.UiEditTypePropertiesItem.Format

sealed class SpacePropertiesEvent {

    data class OnPropertyNameUpdate(val name: String) : SpacePropertiesEvent()
    data object OnSaveButtonClicked : SpacePropertiesEvent()
    data object OnCreateNewButtonClicked : SpacePropertiesEvent()

    data object OnPropertyFormatClick : SpacePropertiesEvent()
    data object OnPropertyFormatsListDismiss : SpacePropertiesEvent()
    data class OnPropertyFormatSelected(val format: Format) : SpacePropertiesEvent()

    data object OnLimitTypesClick : SpacePropertiesEvent()
    data object OnLimitTypesDismiss : SpacePropertiesEvent()
    data class OnLimitTypesDoneClick(val items: List<Id>) : SpacePropertiesEvent()
}
