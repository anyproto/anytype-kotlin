package com.anytypeio.anytype.feature_date.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.foundation.components.BottomNavigationMenu
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.feature_date.R
import com.anytypeio.anytype.feature_date.models.DateObjectBottomMenu
import com.anytypeio.anytype.feature_date.models.DateObjectHeaderState
import com.anytypeio.anytype.feature_date.models.DateObjectHorizontalListState
import com.anytypeio.anytype.feature_date.models.DateObjectTopToolbarState
import com.anytypeio.anytype.feature_date.models.DateObjectVerticalListState
import com.anytypeio.anytype.feature_date.models.UiHorizontalListItem
import com.anytypeio.anytype.feature_date.models.UiVerticalListItem

@Composable
fun DateObjectScreen(
    uiTopToolbarState: DateObjectTopToolbarState,
    uiHeaderState: DateObjectHeaderState,
    uiHorizontalListState: DateObjectHorizontalListState,
    uiVerticalListState: DateObjectVerticalListState,
    uiDateObjectBottomMenu: DateObjectBottomMenu,
    uiHeaderActions: (DateObjectHeaderState.Action) -> Unit,
    uiTopToolbarActions: (DateObjectTopToolbarState.Action) -> Unit,
    uiHorizontalListActions: (UiHorizontalListItem) -> Unit,
    uiVerticalListActions: (UiVerticalListItem) -> Unit,
    uiBottomMenuActions: (DateObjectBottomMenu.Action) -> Unit
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colorResource(id = R.color.background_primary),
        topBar = {
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
        },
        content = { paddingValues ->
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
                    DateLayoutHorizontalListScreen(uiHorizontalListState, uiHorizontalListActions)
                }
                BottomNavigationMenu(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    backClick = { uiBottomMenuActions(DateObjectBottomMenu.Action.Back) },
                    backLongClick = { uiBottomMenuActions(DateObjectBottomMenu.Action.BackLong) },
                    searchClick  = { uiBottomMenuActions(DateObjectBottomMenu.Action.GlobalSearch) },
                    addDocClick = { uiBottomMenuActions(DateObjectBottomMenu.Action.AddDoc) },
                    addDocLongClick = { uiBottomMenuActions(DateObjectBottomMenu.Action.CreateObjectLong) },
                    isOwnerOrEditor = uiDateObjectBottomMenu.isOwnerOrEditor
                )
            }
        }
    )
}