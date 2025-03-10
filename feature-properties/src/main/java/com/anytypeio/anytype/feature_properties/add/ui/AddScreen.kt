package com.anytypeio.anytype.feature_properties.add.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.anytypeio.anytype.feature_properties.add.UiEditTypePropertiesEvent
import com.anytypeio.anytype.feature_properties.add.UiEditTypePropertiesState
import com.anytypeio.anytype.feature_properties.edit.UiEditPropertyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFieldScreen(
    state: UiEditTypePropertiesState,
    uiStateEditProperty: UiEditPropertyState,
    event: (UiEditTypePropertiesEvent) -> Unit
) {
}