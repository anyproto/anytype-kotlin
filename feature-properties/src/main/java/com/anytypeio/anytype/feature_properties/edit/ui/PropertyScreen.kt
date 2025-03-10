package com.anytypeio.anytype.feature_properties.edit.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anytypeio.anytype.feature_properties.edit.UiEditPropertyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyScreen(
    modifier: Modifier,
    uiState: UiEditPropertyState.Visible,
    onSaveButtonClicked: () -> Unit = {},
    onFormatClick: () -> Unit = {},
    onLimitTypesClick: () -> Unit = {},
    onCreateNewButtonClicked: () -> Unit = {},
    onDismissRequest: () -> Unit,
    onPropertyNameUpdate: (String) -> Unit
) {
}