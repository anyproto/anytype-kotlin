package com.anytypeio.anytype.feature_object_type.ui

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.anytypeio.anytype.core_ui.extensions.swapList
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.lists.objects.ListItemLoading
import com.anytypeio.anytype.core_ui.lists.objects.ObjectsListItem
import com.anytypeio.anytype.core_ui.lists.objects.UiContentState
import com.anytypeio.anytype.core_ui.lists.objects.UiObjectsListState
import com.anytypeio.anytype.core_ui.syncstatus.SpaceSyncStatusScreen
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.feature_object_type.R
import com.anytypeio.anytype.feature_object_type.models.UiDeleteAlertState
import com.anytypeio.anytype.feature_object_type.models.UiEditButton
import com.anytypeio.anytype.feature_object_type.models.UiFieldsButtonState
import com.anytypeio.anytype.feature_object_type.models.UiIconState
import com.anytypeio.anytype.feature_object_type.models.UiLayoutButtonState
import com.anytypeio.anytype.feature_object_type.models.UiLayoutTypeState
import com.anytypeio.anytype.feature_object_type.models.UiMenuSetItem
import com.anytypeio.anytype.feature_object_type.models.UiMenuState
import com.anytypeio.anytype.feature_object_type.models.UiObjectsAddIconState
import com.anytypeio.anytype.feature_object_type.models.UiObjectsHeaderState
import com.anytypeio.anytype.feature_object_type.models.UiObjectsSettingsIconState
import com.anytypeio.anytype.feature_object_type.models.UiSyncStatusBadgeState
import com.anytypeio.anytype.feature_object_type.models.UiTemplatesAddIconState
import com.anytypeio.anytype.feature_object_type.models.UiTemplatesHeaderState
import com.anytypeio.anytype.feature_object_type.models.UiTemplatesListState
import com.anytypeio.anytype.feature_object_type.models.UiTitleState
import com.anytypeio.anytype.feature_object_type.ui.alerts.DeleteAlertScreen
import com.anytypeio.anytype.feature_object_type.ui.header.HorizontalButtons
import com.anytypeio.anytype.feature_object_type.ui.header.IconAndTitleWidget
import com.anytypeio.anytype.feature_object_type.ui.header.TopToolbar
import com.anytypeio.anytype.feature_object_type.ui.layouts.TypeLayoutsScreen
import com.anytypeio.anytype.feature_object_type.ui.objects.ObjectsHeader
import com.anytypeio.anytype.feature_object_type.ui.templates.TemplatesScreen
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.UiObjectsListItem
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState
import com.anytypeio.anytype.presentation.templates.TemplateView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectTypeMainScreen(
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
    //templates header
    uiTemplatesHeaderState: UiTemplatesHeaderState,
    uiTemplatesAddIconState: UiTemplatesAddIconState,
    //templates list
    uiTemplatesListState: UiTemplatesListState,
    //objects header
    uiObjectsHeaderState: UiObjectsHeaderState,
    uiObjectsAddIconState: UiObjectsAddIconState,
    uiObjectsSettingsIconState: UiObjectsSettingsIconState,
    uiObjectsMenuState: UiMenuState,
    //objects list
    uiObjectsListState: UiObjectsListState,
    uiContentState: UiContentState,
    //delete alert
    uiDeleteAlertState: UiDeleteAlertState,
    //events
    onTypeEvent: (TypeEvent) -> Unit
) {

    val objects = remember { mutableStateListOf<UiObjectsListItem>() }
    objects.swapList(uiObjectsListState.items)

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
            MainContent(
                paddingValues = paddingValues,
                uiIconState = uiIconState,
                uiTitleState = uiTitleState,
                uiFieldsButtonState = uiFieldsButtonState,
                uiLayoutButtonState = uiLayoutButtonState,
                uiTemplatesHeaderState = uiTemplatesHeaderState,
                uiTemplatesAddIconState = uiTemplatesAddIconState,
                uiTemplatesListState = uiTemplatesListState,
                uiObjectsHeaderState = uiObjectsHeaderState,
                uiObjectsAddIconState = uiObjectsAddIconState,
                uiObjectsSettingsIconState = uiObjectsSettingsIconState,
                uiObjectsMenuState = uiObjectsMenuState,
                objects = objects,
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
}

@Composable
private fun MainContent(
    paddingValues: PaddingValues,
    uiIconState: UiIconState,
    uiTitleState: UiTitleState,
    uiFieldsButtonState: UiFieldsButtonState,
    uiLayoutButtonState: UiLayoutButtonState,
    uiTemplatesHeaderState: UiTemplatesHeaderState,
    uiTemplatesAddIconState: UiTemplatesAddIconState,
    uiTemplatesListState: UiTemplatesListState,
    uiObjectsHeaderState: UiObjectsHeaderState,
    uiObjectsAddIconState: UiObjectsAddIconState,
    uiObjectsSettingsIconState: UiObjectsSettingsIconState,
    uiObjectsMenuState: UiMenuState,
    objects: List<UiObjectsListItem>,
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

    LazyColumn(modifier = contentModifier) {
        item {
            IconAndTitleWidget(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 32.dp)
                    .padding(horizontal = 20.dp),
                uiIconState = uiIconState,
                uiTitleState = uiTitleState,
                onTypeEvent = onTypeEvent
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
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

        if (uiTemplatesHeaderState is UiTemplatesHeaderState.Visible) {
            item {
                TemplatesScreen(
                    uiTemplatesHeaderState = uiTemplatesHeaderState,
                    uiTemplatesAddIconState = uiTemplatesAddIconState,
                    uiTemplatesListState = uiTemplatesListState,
                    onTypeEvent = onTypeEvent
                )
            }
        }

        item {
            ObjectsHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 20.dp),
                uiObjectsHeaderState = uiObjectsHeaderState,
                uiObjectsAddIconState = uiObjectsAddIconState,
                uiObjectsSettingsIconState = uiObjectsSettingsIconState,
                uiObjectsMenuState = uiObjectsMenuState,
                onTypeEvent = onTypeEvent
            )
        }

        if (objects.isEmpty()) {
            item {
                EmptyScreen(
                    modifier = Modifier.padding(top = 18.dp)
                )
            }
        } else {
            items(
                count = objects.size,
                key = { index -> objects[index].id },
                contentType = { index ->
                    when (objects[index]) {
                        is UiObjectsListItem.Loading -> "loading"
                        is UiObjectsListItem.Item -> "item"
                    }
                }
            ) { index ->
                when (val item = objects[index]) {
                    is UiObjectsListItem.Item -> {
                        ObjectsListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                                .noRippleThrottledClickable {
                                    onTypeEvent(TypeEvent.OnObjectItemClick(item))
                                },
                            item = item
                        )
                        Divider(paddingStart = 20.dp, paddingEnd = 20.dp)
                    }
                    is UiObjectsListItem.Loading -> {
                        ListItemLoading(modifier = Modifier)
                    }
                }
            }
        }

        // Objects menu actions
        when (val itemSet = uiObjectsMenuState.objSetItem) {
            UiMenuSetItem.CreateSet -> {
                item {
                    ButtonSecondary(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, start = 20.dp, end = 20.dp),
                        text = stringResource(R.string.object_type_objects_menu_create_set),
                        size = ButtonSize.Large,
                        onClick = { onTypeEvent(TypeEvent.OnCreateSetClick) }
                    )
                }
            }
            is UiMenuSetItem.OpenSet -> {
                item {
                    ButtonSecondary(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, start = 20.dp, end = 20.dp),
                        text = stringResource(R.string.object_type_objects_menu_open_set),
                        size = ButtonSize.Large,
                        onClick = { onTypeEvent(TypeEvent.OnOpenSetClick(setId = itemSet.setId)) }
                    )
                }
            }
            UiMenuSetItem.Hidden -> Unit
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarContent(
    uiSyncStatusBadgeState: UiSyncStatusBadgeState,
    uiEditButtonState: UiEditButton,
    uiTitleState: UiTitleState,
    topBarScrollBehavior: TopAppBarScrollBehavior,
    onTypeEvent: (TypeEvent) -> Unit
) {
    // Use windowInsetsPadding if running on a recent SDK
    val modifier = if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
        Modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .fillMaxWidth()
    } else {
        Modifier.fillMaxWidth()
    }

    Column(modifier = modifier) {
        TopToolbar(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            uiSyncStatusBadgeState = uiSyncStatusBadgeState,
            uiEditButtonState = uiEditButtonState,
            uiTitleState = uiTitleState,
            onTypeEvent = onTypeEvent,
            topBarScrollBehavior = topBarScrollBehavior
        )
    }
}

@Composable
private fun BottomSyncStatus(
    uiSyncStatusState: SyncStatusWidgetState,
    onDismiss: () -> Unit
) {
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
            onDismiss = onDismiss,
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
        uiTemplatesHeaderState = UiTemplatesHeaderState.Visible(count = "3"),
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
                TemplateView.New(
                    targetTypeId = TypeId("32423"),
                    targetTypeKey = TypeKey("43232")
                )
            )
        ),
        uiObjectsAddIconState = UiObjectsAddIconState.Visible,
        uiObjectsHeaderState = UiObjectsHeaderState(count = "3"),
        uiObjectsSettingsIconState = UiObjectsSettingsIconState.Visible,
        uiObjectsListState = UiObjectsListState(emptyList()),
        uiContentState = UiContentState.Idle(),
        uiObjectsMenuState = UiMenuState.EMPTY,
        uiDeleteAlertState = UiDeleteAlertState.Hidden,
        uiEditButtonState = UiEditButton.Visible,
        uiLayoutTypeState = UiLayoutTypeState.Hidden,
        onTypeEvent = {}
    )
}

