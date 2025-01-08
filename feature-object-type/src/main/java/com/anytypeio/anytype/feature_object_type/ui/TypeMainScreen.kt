package com.anytypeio.anytype.feature_object_type.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncError
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncNetwork
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.lists.objects.ObjectsScreen
import com.anytypeio.anytype.core_ui.lists.objects.UiContentState
import com.anytypeio.anytype.core_ui.lists.objects.UiObjectsListState
import com.anytypeio.anytype.core_ui.syncstatus.SpaceSyncStatusScreen
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.feature_object_type.R
import com.anytypeio.anytype.feature_object_type.ui.header.HorizontalButtons
import com.anytypeio.anytype.feature_object_type.ui.header.IconAndTitleWidget
import com.anytypeio.anytype.feature_object_type.ui.header.TopToolbar
import com.anytypeio.anytype.feature_object_type.ui.objects.ObjectsHeader
import com.anytypeio.anytype.feature_object_type.ui.templates.TemplatesHeader
import com.anytypeio.anytype.feature_object_type.ui.templates.TemplatesList
import com.anytypeio.anytype.feature_object_type.viewmodel.UiFieldsButtonState
import com.anytypeio.anytype.feature_object_type.viewmodel.UiIconState
import com.anytypeio.anytype.feature_object_type.viewmodel.UiLayoutButtonState
import com.anytypeio.anytype.feature_object_type.viewmodel.UiObjectsAddIconState
import com.anytypeio.anytype.feature_object_type.viewmodel.UiObjectsHeaderState
import com.anytypeio.anytype.feature_object_type.viewmodel.UiObjectsSettingsIconState
import com.anytypeio.anytype.feature_object_type.viewmodel.UiSyncStatusBadgeState
import com.anytypeio.anytype.feature_object_type.viewmodel.UiTemplatesAddIconState
import com.anytypeio.anytype.feature_object_type.viewmodel.UiTemplatesHeaderState
import com.anytypeio.anytype.feature_object_type.viewmodel.UiTemplatesListState
import com.anytypeio.anytype.feature_object_type.viewmodel.UiTitleState
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState
import com.anytypeio.anytype.presentation.templates.TemplateView

@Composable
fun ObjectTypeMainScreen(

    //sync status
    uiSyncStatusBadgeState: UiSyncStatusBadgeState,
    uiSyncStatusState: SyncStatusWidgetState,

    //header
    uiIconState: UiIconState,
    uiTitleState: UiTitleState,

    //layout and fields buttons
    uiFieldsButtonState: UiFieldsButtonState,
    uiLayoutButtonState: UiLayoutButtonState,

    //templates header
    uiTemplatesHeaderState: UiTemplatesHeaderState,
    uiTemplatesAddIconState: UiTemplatesAddIconState,

    //templates list
    uiTemplatesListState: UiTemplatesListState,

    //objects header
    uiObjectsHeaderState: UiObjectsHeaderState,
    uiObjectsAddIconState: UiObjectsAddIconState,
    uiObjectsSettingsIconState: UiObjectsSettingsIconState,

    //objects list
    uiObjectsListState: UiObjectsListState,
    uiContentState: UiContentState,

    onTypeEvent: (TypeEvent) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colorResource(id = R.color.background_primary),
        contentColor = colorResource(id = R.color.background_primary),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colorResource(id = R.color.background_primary))
            ) {
                if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
                    Spacer(
                        modifier = Modifier.windowInsetsTopHeight(
                            WindowInsets.statusBars
                        )
                    )
                }
                TopToolbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    uiSyncStatusBadgeState = uiSyncStatusBadgeState,
                    onTypeEvent = onTypeEvent
                )
                Spacer(
                    modifier = Modifier.height(32.dp)
                )
                IconAndTitleWidget(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
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
                    onTypeEvent = onTypeEvent
                )
            }
        },
        content = { paddingValues ->
            val contentModifier =
                if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                    Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
                else
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
            Column(
                modifier = contentModifier,
            ) {
                Spacer(
                    modifier = Modifier.height(44.dp)
                )
                TemplatesHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    uiTemplatesHeaderState = uiTemplatesHeaderState,
                    uiTemplatesAddIconState = uiTemplatesAddIconState,
                    onTypeEvent = onTypeEvent
                )
                Spacer(
                    modifier = Modifier.height(12.dp)
                )
                TemplatesList(
                    uiTemplatesListState = uiTemplatesListState
                )
                Spacer(
                    modifier = Modifier.height(32.dp)
                )
                ObjectsHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    uiObjectsHeaderState = uiObjectsHeaderState,
                    uiObjectsAddIconState = uiObjectsAddIconState,
                    uiObjectsSettingsIconState = uiObjectsSettingsIconState,
                    onTypeEvent = onTypeEvent
                )
                if (uiObjectsListState.items.isEmpty()) {
                    EmptyScreen(
                        modifier = Modifier
                            .padding(top = 18.dp)
                    )
                }
                ObjectsScreen(
                    state = uiObjectsListState,
                    uiState = uiContentState,
                    canPaginate = false,
                    onLoadMore = {

                    },
                    onMoveToBin = { item ->

                    },
                    onObjectClicked = { item ->

                    }
                )
            }
        }
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        SpaceSyncStatusScreen(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .windowInsetsPadding(WindowInsets.navigationBars),
            modifierCard = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 16.dp),
            uiState = uiSyncStatusState,
            onDismiss = { onTypeEvent(TypeEvent.OnSyncStatusDismiss) },
            onUpdateAppClick = {}
        )
    }
}

@Composable
fun EmptyScreen(modifier: Modifier) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            text = stringResource(R.string.object_type_empty_items_title),
            color = colorResource(id = R.color.text_secondary),
            style = Title2,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            text = stringResource(R.string.object_type_empty_items_subtitle),
            color = colorResource(id = R.color.text_secondary),
            style = Relations3,
            textAlign = TextAlign.Center
        )
    }
}

@DefaultPreviews
@Composable
fun ObjectTypeMainScreenPreview() {
    val spaceSyncUpdate = SpaceSyncUpdate.Update(
        id = "1",
        status = SpaceSyncStatus.SYNCING,
        network = SpaceSyncNetwork.ANYTYPE,
        error = SpaceSyncError.NULL,
        syncingObjectsCounter = 2
    )
    ObjectTypeMainScreen(
        uiSyncStatusBadgeState = UiSyncStatusBadgeState.Visible(
            status = SpaceSyncAndP2PStatusState.Success(
                spaceSyncUpdate = spaceSyncUpdate,
                p2PStatusUpdate = P2PStatusUpdate.Initial
            )
        ),
        uiSyncStatusState = SyncStatusWidgetState.Hidden,
        uiIconState = UiIconState(icon = ObjectIcon.Empty.Page, isEditable = true),
        uiTitleState = UiTitleState(title = "title", isEditable = true),
        uiFieldsButtonState = UiFieldsButtonState.Visible(4),
        uiLayoutButtonState = UiLayoutButtonState.Visible(layout = ObjectType.Layout.VIDEO),
        uiTemplatesHeaderState = UiTemplatesHeaderState(count = "3"),
        uiTemplatesAddIconState = UiTemplatesAddIconState.Visible,
        uiTemplatesListState = UiTemplatesListState(
            items = listOf(
                TemplateView.Template(
                    id = "1",
                    name = "Template 1",
                    targetTypeId = TypeId("page"),
                    targetTypeKey = TypeKey("ot-page"),
                    layout = ObjectType.Layout.BASIC,
                    image = null,
                    emoji = ":)",
                    coverColor = CoverColor.RED,
                    coverGradient = null,
                    coverImage = null,
                ),
                TemplateView.Template(
                    id = "2",
                    name = "Template 2",
                    targetTypeId = TypeId("note"),
                    targetTypeKey = TypeKey("ot-note"),
                    layout = ObjectType.Layout.NOTE,
                    image = null,
                    emoji = null,
                    coverColor = null,
                    coverGradient = null,
                    coverImage = null,
                ),
            )
        ),
        uiObjectsAddIconState = UiObjectsAddIconState.Visible,
        uiObjectsHeaderState = UiObjectsHeaderState(count = "3"),
        uiObjectsSettingsIconState = UiObjectsSettingsIconState.Visible,
        uiObjectsListState = UiObjectsListState(emptyList()),
        uiContentState = UiContentState.Idle(),
        onTypeEvent = {}
    )
}

