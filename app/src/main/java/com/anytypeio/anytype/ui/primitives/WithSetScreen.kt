package com.anytypeio.anytype.ui.primitives

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent
import com.anytypeio.anytype.feature_object_type.ui.UiDeleteAlertState
import com.anytypeio.anytype.feature_object_type.ui.UiEditButton
import com.anytypeio.anytype.feature_object_type.ui.UiFieldsButtonState
import com.anytypeio.anytype.feature_object_type.ui.UiIconState
import com.anytypeio.anytype.feature_object_type.ui.UiLayoutButtonState
import com.anytypeio.anytype.feature_object_type.ui.UiLayoutTypeState
import com.anytypeio.anytype.feature_object_type.ui.UiSyncStatusBadgeState
import com.anytypeio.anytype.feature_object_type.ui.UiTemplatesButtonState
import com.anytypeio.anytype.feature_object_type.ui.UiTemplatesModalListState
import com.anytypeio.anytype.feature_object_type.ui.UiTitleState
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithSetScreen(
    //top bar
    uiEditButtonState: UiEditButton,
    uiSyncStatusBadgeState: UiSyncStatusBadgeState,
    uiSyncStatusState: SyncStatusWidgetState,
    //header
    uiIconState: UiIconState,
    uiTitleState: UiTitleState,
    //layout and fields buttons
    uiFieldsButtonState: UiFieldsButtonState,
    uiLayoutButtonState: UiLayoutButtonState,
    uiLayoutTypeState: UiLayoutTypeState,
    uiTemplatesButtonState: UiTemplatesButtonState,
    //templates modal list
    uiTemplatesModalListState: UiTemplatesModalListState,
    //delete alert
    uiDeleteAlertState: UiDeleteAlertState,
    //events
    onTypeEvent: (TypeEvent) -> Unit,
    objectId: String,
    space: String,
) {
    //next PR
}