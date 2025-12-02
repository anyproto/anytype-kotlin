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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.fragment.compose.AndroidFragment
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.feature_object_type.R
import com.anytypeio.anytype.feature_object_type.ui.BottomSyncStatus
import com.anytypeio.anytype.feature_object_type.ui.TopBarContent
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent
import com.anytypeio.anytype.feature_object_type.ui.UiDeleteAlertState
import com.anytypeio.anytype.feature_object_type.ui.UiEditButton
import com.anytypeio.anytype.feature_object_type.ui.UiHorizontalButtonsState
import com.anytypeio.anytype.feature_object_type.ui.UiIconState
import com.anytypeio.anytype.feature_object_type.ui.UiLayoutTypeState
import com.anytypeio.anytype.feature_object_type.ui.UiSyncStatusBadgeState
import com.anytypeio.anytype.feature_object_type.ui.UiTemplatesModalListState
import com.anytypeio.anytype.feature_object_type.ui.UiTitleState
import com.anytypeio.anytype.feature_object_type.ui.UiDescriptionState
import com.anytypeio.anytype.feature_object_type.ui.alerts.DeleteAlertScreen
import com.anytypeio.anytype.feature_object_type.ui.header.DescriptionWidget
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
    uiSyncStatusBadgeState: UiSyncStatusBadgeState,
    uiSyncStatusState: SyncStatusWidgetState,
    //header
    uiIconState: UiIconState,
    uiTitleState: UiTitleState,
    uiDescriptionState: UiDescriptionState,
    //layout, properties and templates buttons
    uiHorizontalButtonsState: UiHorizontalButtonsState,
    uiLayoutTypeState: UiLayoutTypeState,
    //templates modal list
    uiTemplatesModalListState: UiTemplatesModalListState,
    //delete alert
    uiDeleteAlertState: UiDeleteAlertState,
    //events
    onTypeEvent: (TypeEvent) -> Unit,
    objectId: String,
    space: String,
    view: String? = null,
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )
    val objectSetFragment: MutableState<ObjectSetFragment?> = remember { mutableStateOf(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        containerColor = colorResource(id = R.color.background_primary),
        contentColor = colorResource(id = R.color.background_primary),
        topBar = {
            TopBarContent(
                uiSyncStatusBadgeState = uiSyncStatusBadgeState,
                onTypeEvent = { typeEvent ->
                    if (typeEvent is TypeEvent.OnBackClick) {
                        objectSetFragment.value?.onCloseCurrentObject()
                    }
                    if (typeEvent is TypeEvent.OnMenuClick) {
                        objectSetFragment.value?.onMenuClicked()
                    }
                    onTypeEvent(typeEvent)
                }
            )
        },
        content = { paddingValues ->
            MainContentSet(
                paddingValues = paddingValues,
                uiIconState = uiIconState,
                uiTitleState = uiTitleState,
                uiDescriptionState = uiDescriptionState,
                uiHorizontalButtonsState = uiHorizontalButtonsState,
                objectId = objectId,
                space = space,
                view = view,
                onTypeEvent = onTypeEvent,
                objectSetFragment = objectSetFragment
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
    uiDescriptionState: UiDescriptionState,
    uiHorizontalButtonsState: UiHorizontalButtonsState,
    objectId: String,
    space: String,
    view: String?,
    onTypeEvent: (TypeEvent) -> Unit,
    objectSetFragment: MutableState<ObjectSetFragment?>
) {
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

        if (uiDescriptionState.isVisible) {
            Spacer(modifier = Modifier.height(8.dp))
            DescriptionWidget(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 20.dp),
                uiDescriptionState = uiDescriptionState,
                onDescriptionChanged = { text ->
                    onTypeEvent(TypeEvent.OnDescriptionChanged(text))
                }
            )
        }

        if (uiHorizontalButtonsState.isVisible) {
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalButtons(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .padding(horizontal = 20.dp),
                uiPropertiesButtonState = uiHorizontalButtonsState.uiPropertiesButtonState,
                uiLayoutButtonState = uiHorizontalButtonsState.uiLayoutButtonState,
                uiTemplatesButtonState = uiHorizontalButtonsState.uiTemplatesButtonState,
                onTypeEvent = onTypeEvent
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        AndroidFragment<ObjectSetFragment>(
            modifier = Modifier
                .fillMaxSize(),
            arguments = ObjectSetFragment.args(
                ctx = objectId,
                space = space,
                view = view
            )
        ) { fragment ->
            objectSetFragment.value = fragment
            fragment.view?.findViewById<View>(R.id.topToolbar)?.gone()
            fragment.view?.findViewById<View>(R.id.objectHeader)?.visibility =
                View.GONE
        }
    }
}