package com.anytypeio.anytype.ui.primitives

import android.os.Build
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.fragment.compose.AndroidFragment
import com.anytypeio.anytype.core_ui.lists.objects.UiContentState
import com.anytypeio.anytype.core_ui.lists.objects.UiObjectsListState
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.feature_object_type.R
import com.anytypeio.anytype.feature_object_type.ui.BottomSyncStatus
import com.anytypeio.anytype.feature_object_type.ui.TopBarContent
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent
import com.anytypeio.anytype.feature_object_type.ui.UiDeleteAlertState
import com.anytypeio.anytype.feature_object_type.ui.UiEditButton
import com.anytypeio.anytype.feature_object_type.ui.UiFieldsButtonState
import com.anytypeio.anytype.feature_object_type.ui.UiIconState
import com.anytypeio.anytype.feature_object_type.ui.UiLayoutButtonState
import com.anytypeio.anytype.feature_object_type.ui.UiLayoutTypeState
import com.anytypeio.anytype.feature_object_type.ui.UiMenuState
import com.anytypeio.anytype.feature_object_type.ui.UiObjectsAddIconState
import com.anytypeio.anytype.feature_object_type.ui.UiObjectsHeaderState
import com.anytypeio.anytype.feature_object_type.ui.UiObjectsSettingsIconState
import com.anytypeio.anytype.feature_object_type.ui.UiSyncStatusBadgeState
import com.anytypeio.anytype.feature_object_type.ui.UiTemplatesAddIconState
import com.anytypeio.anytype.feature_object_type.ui.UiTemplatesButtonState
import com.anytypeio.anytype.feature_object_type.ui.UiTemplatesHeaderState
import com.anytypeio.anytype.feature_object_type.ui.UiTemplatesListState
import com.anytypeio.anytype.feature_object_type.ui.UiTemplatesModalListState
import com.anytypeio.anytype.feature_object_type.ui.UiTitleState
import com.anytypeio.anytype.feature_object_type.ui.alerts.DeleteAlertScreen
import com.anytypeio.anytype.feature_object_type.ui.header.HorizontalButtons
import com.anytypeio.anytype.feature_object_type.ui.header.IconAndTitleWidget
import com.anytypeio.anytype.feature_object_type.ui.layouts.TypeLayoutsScreen
import com.anytypeio.anytype.feature_object_type.ui.templates.TemplatesModalList
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState
import com.anytypeio.anytype.ui.sets.ObjectSetFragment

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
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        containerColor = colorResource(id = R.color.background_primary),
        contentColor = colorResource(id = R.color.background_primary),
        topBar = {
            TopBarContent(
                uiSyncStatusBadgeState = uiSyncStatusBadgeState,
                uiEditButtonState = uiEditButtonState,
                uiTitleState = uiTitleState,
                topBarScrollBehavior = topAppBarScrollBehavior,
                onTypeEvent = onTypeEvent
            )
        },
        content = { paddingValues ->
            MainContentSet(
                paddingValues = paddingValues,
                uiIconState = uiIconState,
                uiTitleState = uiTitleState,
                uiFieldsButtonState = uiFieldsButtonState,
                uiLayoutButtonState = uiLayoutButtonState,
                uiTemplatesButtonState = uiTemplatesButtonState,
                objectId = objectId,
                space = space,
                onTypeEvent = onTypeEvent
            )
        }
    )

    BottomSyncStatus(
        uiSyncStatusState = uiSyncStatusState,
        onDismiss = { onTypeEvent(TypeEvent.OnSyncStatusDismiss) }
    )

    if (uiDeleteAlertState is UiDeleteAlertState.Show) {
        DeleteAlertScreen(
            onTypeEvent = onTypeEvent
        )
    }

    if (uiLayoutTypeState is UiLayoutTypeState.Visible) {
        TypeLayoutsScreen(
            modifier = Modifier.fillMaxWidth(),
            uiState = uiLayoutTypeState,
            onTypeEvent = onTypeEvent
        )
    }

    if (uiTemplatesModalListState is UiTemplatesModalListState.Visible) {
        TemplatesModalList(
            modifier = Modifier.fillMaxWidth(),
            uiState = uiTemplatesModalListState,
            onTypeEvent = onTypeEvent
        )
    }
}


@Composable
private fun MainContentSet(
    paddingValues: PaddingValues,
    uiIconState: UiIconState,
    uiTitleState: UiTitleState,
    uiFieldsButtonState: UiFieldsButtonState,
    uiLayoutButtonState: UiLayoutButtonState,
    uiTemplatesButtonState: UiTemplatesButtonState,
    objectId: String,
    space: String,
    onTypeEvent: (TypeEvent) -> Unit
) {
    // Adjust content modifier based on SDK version for proper insets handling
    val contentModifier = if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
        Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding())
    } else {
        Modifier
            .fillMaxSize()
            .padding(paddingValues)
    }

    Column(modifier = contentModifier) {
        IconAndTitleWidget(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 35.dp)
                .padding(horizontal = 20.dp),
            uiIconState = uiIconState,
            uiTitleState = uiTitleState,
            onTypeEvent = onTypeEvent
        )
        Spacer(modifier = Modifier.height(20.dp))
        HorizontalButtons(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .padding(horizontal = 20.dp),
            uiFieldsButtonState = uiFieldsButtonState,
            uiLayoutButtonState = uiLayoutButtonState,
            uiTemplatesButtonState = uiTemplatesButtonState,
            onTypeEvent = onTypeEvent
        )
        Spacer(modifier = Modifier.height(24.dp))
        AndroidFragment<ObjectSetFragment>(
            modifier = Modifier
                .fillMaxSize(),
            //.padding(paddingValues),
            arguments = ObjectSetFragment.args(
                ctx = objectId,
                space = space
            )
        ) { fragment ->
            fragment.view?.findViewById<View>(R.id.objectHeader)?.visibility =
                View.GONE
        }
    }


}