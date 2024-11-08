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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.foundation.components.BottomNavigationMenu
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.feature_date.R
import com.anytypeio.anytype.feature_date.models.DateObjectBottomMenu
import com.anytypeio.anytype.feature_date.models.DateObjectHeaderState
import com.anytypeio.anytype.feature_date.models.DateObjectHorizontalListState
import com.anytypeio.anytype.feature_date.models.DateObjectSheetState
import com.anytypeio.anytype.feature_date.models.DateObjectTopToolbarState
import com.anytypeio.anytype.feature_date.models.DateObjectVerticalListState
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
    uiBottomMenuActions: (DateObjectBottomMenu.Action) -> Unit
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
            TopBar(
                uiTopToolbarState = uiTopToolbarState,
                uiHeaderState = uiHeaderState,
                uiTopToolbarActions = uiTopToolbarActions,
                uiHeaderActions = uiHeaderActions
            )
        },
        content = { paddingValues ->
            MainContent(
                paddingValues = paddingValues,
                uiHorizontalListState = uiHorizontalListState,
                uiVerticalListState = uiVerticalListState,
                uiDateObjectBottomMenu = uiDateObjectBottomMenu,
                uiHorizontalListActions = {
                    if (it is UiHorizontalListItem.Settings) {
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    }
                    uiHorizontalListActions(it)
                },
                uiVerticalListActions = uiVerticalListActions,
                uiBottomMenuActions = uiBottomMenuActions
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
private fun TopBar(
    uiTopToolbarState: DateObjectTopToolbarState,
    uiHeaderState: DateObjectHeaderState,
    uiTopToolbarActions: (DateObjectTopToolbarState.Action) -> Unit,
    uiHeaderActions: (DateObjectHeaderState.Action) -> Unit
) {
    val modifier = if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
        Modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .fillMaxWidth()
    else
        Modifier.fillMaxWidth()
    Column {
        if (uiTopToolbarState is DateObjectTopToolbarState.Content) {
            DateLayoutTopToolbar(modifier, uiTopToolbarState, uiTopToolbarActions)
        }
        if (uiHeaderState is DateObjectHeaderState.Content) {
            Spacer(modifier = Modifier.height(24.dp))
            DateLayoutHeader(uiHeaderState, uiHeaderActions)
        }
    }
}

@Composable
private fun MainContent(
    paddingValues: PaddingValues,
    uiHorizontalListState: DateObjectHorizontalListState,
    uiVerticalListState: DateObjectVerticalListState,
    uiDateObjectBottomMenu: DateObjectBottomMenu,
    uiHorizontalListActions: (UiHorizontalListItem) -> Unit,
    uiVerticalListActions: (UiVerticalListItem) -> Unit,
    uiBottomMenuActions: (DateObjectBottomMenu.Action) -> Unit
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
        if (uiHorizontalListState is DateObjectHorizontalListState.Content) {
            DateLayoutHorizontalListScreen(
                state = uiHorizontalListState,
                action = uiHorizontalListActions
            )
        }
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