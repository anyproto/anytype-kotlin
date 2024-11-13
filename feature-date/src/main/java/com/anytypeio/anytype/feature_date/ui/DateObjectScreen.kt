package com.anytypeio.anytype.feature_date.ui

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.components.BottomNavigationMenu
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.feature_date.R
import com.anytypeio.anytype.feature_date.models.DateObjectBottomMenu
import com.anytypeio.anytype.feature_date.models.DateObjectHeaderState
import com.anytypeio.anytype.feature_date.models.DateObjectHorizontalListState
import com.anytypeio.anytype.feature_date.models.DateObjectSheetState
import com.anytypeio.anytype.feature_date.models.DateObjectTopToolbarState
import com.anytypeio.anytype.feature_date.models.DateObjectVerticalListState
import com.anytypeio.anytype.feature_date.models.UiContentState
import com.anytypeio.anytype.feature_date.models.UiHorizontalListItem
import com.anytypeio.anytype.feature_date.models.UiVerticalListItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateObjectScreen(
    uiTopToolbarState: DateObjectTopToolbarState,
    uiHeaderState: DateObjectHeaderState,
    uiHorizontalListState: DateObjectHorizontalListState,
    uiVerticalListState: DateObjectVerticalListState,
    uiDateObjectBottomMenu: DateObjectBottomMenu,
    uiSheetState: DateObjectSheetState,
    uiHeaderActions: (DateObjectHeaderState.Action) -> Unit,
    uiTopToolbarActions: (DateObjectTopToolbarState.Action) -> Unit,
    uiHorizontalListActions: (UiHorizontalListItem) -> Unit,
    uiVerticalListActions: (UiVerticalListItem) -> Unit,
    uiBottomMenuActions: (DateObjectBottomMenu.Action) -> Unit,
    uiContentState: UiContentState,
    canPaginate: Boolean,
    onUpdateLimitSearch: () -> Unit,
) {

    val scope = rememberCoroutineScope()

    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = bottomSheetState
    )

    BackHandler(enabled = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
        scope.launch {
            scaffoldState.bottomSheetState.hide()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        modifier = Modifier.fillMaxSize(),
        containerColor = colorResource(id = R.color.background_primary),
        contentColor = colorResource(id = R.color.background_primary),
        topBar = {
            Column(
                modifier = if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                    Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .fillMaxWidth()
                        .background(color = colorResource(id = R.color.background_primary))
                else
                    Modifier.fillMaxWidth()
                        .background(color = colorResource(id = R.color.background_primary))
            ) {
                DateObjectTopToolbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    state = uiTopToolbarState,
                    action = uiTopToolbarActions
                )
                Spacer(
                    modifier = Modifier.height(24.dp)
                )
                DateObjectHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    state = uiHeaderState,
                    action = uiHeaderActions
                )
                DateLayoutHorizontalListScreen(
                    state = uiHorizontalListState,
                    action = {
                        if (it is UiHorizontalListItem.Settings) {
                            scope.launch {
                                scaffoldState.bottomSheetState.expand()
                            }
                        }
                        uiHorizontalListActions(it)
                    }
                )
                Spacer(
                    modifier = Modifier.height(8.dp)
                )
            }
        },
        content = { paddingValues ->
            MainContent(
                paddingValues = paddingValues,
                uiVerticalListState = uiVerticalListState,
                uiDateObjectBottomMenu = uiDateObjectBottomMenu,
                uiVerticalListActions = uiVerticalListActions,
                uiBottomMenuActions = uiBottomMenuActions,
                uiContentState = uiContentState,
                canPaginate = canPaginate,
                onUpdateLimitSearch = onUpdateLimitSearch
            )
        },
        sheetContent = {
            if (uiSheetState is DateObjectSheetState.Content) {
                DateObjectSheetScreen(
                    uiSheetState = uiSheetState,
                    uiHeaderActions = {},
                    onQueryChange = {}
                )
            }
        }
    )
}

@Composable
private fun MainContent(
    paddingValues: PaddingValues,
    uiVerticalListState: DateObjectVerticalListState,
    uiDateObjectBottomMenu: DateObjectBottomMenu,
    uiVerticalListActions: (UiVerticalListItem) -> Unit,
    uiBottomMenuActions: (DateObjectBottomMenu.Action) -> Unit,
    uiContentState: UiContentState,
    canPaginate: Boolean,
    onUpdateLimitSearch: () -> Unit
) {
    val contentModifier =
        if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
            Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .background(color = colorResource(id = R.color.background_primary))
        else
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = colorResource(id = R.color.background_primary))
    Box(
        modifier = contentModifier,
        contentAlignment = Alignment.TopCenter
    ) {
        DateLayoutVerticalListScreen(
            state = uiVerticalListState,
            uiContentState = uiContentState,
            canPaginate = canPaginate,
            onUpdateLimitSearch = onUpdateLimitSearch,
            uiVerticalListActions = uiVerticalListActions
        )
//        Box(
//            modifier = contentModifier,
//            contentAlignment = Alignment.Center
//        ) {
//            when (uiContentState) {
//                is UiContentState.Empty -> {
//                    EmptyState()
//                }
//                is UiContentState.Error -> {
//                    ErrorState(uiContentState.message)
//                }
//                else -> {
//                    // Do nothing.
//                }
//            }
//        }
        BottomNavigationMenu(
            modifier = Modifier.align(Alignment.BottomCenter),
            backClick = { uiBottomMenuActions(DateObjectBottomMenu.Action.Back) },
            backLongClick = { uiBottomMenuActions(DateObjectBottomMenu.Action.BackLong) },
            searchClick = { uiBottomMenuActions(DateObjectBottomMenu.Action.GlobalSearch) },
            addDocClick = { uiBottomMenuActions(DateObjectBottomMenu.Action.AddDoc) },
            addDocLongClick = { uiBottomMenuActions(DateObjectBottomMenu.Action.CreateObjectLong) },
            isOwnerOrEditor = uiDateObjectBottomMenu.isOwnerOrEditor
        )
    }
}

@Composable
private fun ErrorState(message: String) {
    Column(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            text = stringResource(id = R.string.all_content_error_title),
            color = colorResource(id = R.color.text_primary),
            style = UXBody,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            text = message,
            color = colorResource(id = R.color.palette_system_red),
            style = UXBody,
            textAlign = TextAlign.Center,
            maxLines = 3
        )
    }
}

@DefaultPreviews
@Composable
fun DateObjectScreenPreview() {
    DateObjectScreen(
        uiTopToolbarState = DateObjectTopToolbarState.Content(
            syncStatus = SpaceSyncStatus.SYNCING,
        ),
        uiHeaderState = DateObjectHeaderState.Content(
            title = "06 Nov 2024"
        ),
        uiHorizontalListState = DateObjectHorizontalListState(
            items = StubHorizontalItems,
            selectedRelationKey = null
        ),
        uiVerticalListState = DateObjectVerticalListState(
            items = StubVerticalItems
        ),
        uiDateObjectBottomMenu = DateObjectBottomMenu(isOwnerOrEditor = true),
        uiSheetState = DateObjectSheetState.Content(
            items = emptyList()
        ),
        uiHeaderActions = {},
        uiTopToolbarActions = {},
        uiHorizontalListActions = {},
        uiVerticalListActions = {},
        uiBottomMenuActions = {},
        uiContentState = UiContentState.Idle(),
        canPaginate = false,
        onUpdateLimitSearch = {}
    )
}

@DefaultPreviews
@Composable
fun DateObjectScreenEmptyPreview() {
    DateObjectScreen(
        uiTopToolbarState = DateObjectTopToolbarState.Content(
            syncStatus = SpaceSyncStatus.SYNCING,
        ),
        uiHeaderState = DateObjectHeaderState.Content(
            title = "06 Nov 2024"
        ),
        uiHorizontalListState = DateObjectHorizontalListState.loadingState(),
        uiVerticalListState = DateObjectVerticalListState(
            items = emptyList()
        ),
        uiDateObjectBottomMenu = DateObjectBottomMenu(isOwnerOrEditor = true),
        uiSheetState = DateObjectSheetState.Content(
            items = emptyList()
        ),
        uiHeaderActions = {},
        uiTopToolbarActions = {},
        uiHorizontalListActions = {},
        uiVerticalListActions = {},
        uiBottomMenuActions = {},
        uiContentState = UiContentState.Empty,
        canPaginate = false,
        onUpdateLimitSearch = {}
    )
}