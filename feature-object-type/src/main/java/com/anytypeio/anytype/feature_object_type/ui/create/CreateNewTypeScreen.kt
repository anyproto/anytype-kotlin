package com.anytypeio.anytype.feature_object_type.ui.create

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetTypeTitlesAndIconScreen(
    uiState: UiTypeSetupTitleAndIconState,
    modifier: Modifier = Modifier,
    onTitleChanged: (String) -> Unit,
    onPluralChanged: (String) -> Unit,
    onIconClicked: () -> Unit,
    onDismiss: () -> Unit,
    onButtonClicked: () -> Unit
) {
    //next PR
}