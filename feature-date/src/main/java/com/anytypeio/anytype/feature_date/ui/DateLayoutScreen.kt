package com.anytypeio.anytype.feature_date.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
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
import com.anytypeio.anytype.feature_date.models.DateLayoutBottomMenu
import com.anytypeio.anytype.feature_date.models.DateLayoutHeaderState
import com.anytypeio.anytype.feature_date.models.DateLayoutHorizontalListState
import com.anytypeio.anytype.feature_date.models.DateLayoutTopToolbarState
import com.anytypeio.anytype.feature_date.models.DateLayoutVerticalListState
import com.anytypeio.anytype.feature_date.models.UiHorizontalListItem
import com.anytypeio.anytype.feature_date.models.UiVerticalListItem

@Composable
fun DateLayoutScreen(
    uiTopToolbarState: DateLayoutTopToolbarState,
    uiHeaderState: DateLayoutHeaderState,
    uiHorizontalListState: DateLayoutHorizontalListState,
    uiVerticalListState: DateLayoutVerticalListState,
    uiDateLayoutBottomMenu: DateLayoutBottomMenu,
    uiHeaderActions: (DateLayoutHeaderState.Action) -> Unit,
    uiTopToolbarActions: (DateLayoutTopToolbarState.Action) -> Unit,
    uiHorizontalListActions: (UiHorizontalListItem) -> Unit,
    uiVerticalListActions: (UiVerticalListItem) -> Unit,
    uiBottomMenuActions: (DateLayoutBottomMenu.Action) -> Unit
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colorResource(id = R.color.background_primary),
        bottomBar = {
            BottomMenu(
                uiBottomMenuActions = uiBottomMenuActions,
                uiDateLayoutBottomMenu = uiDateLayoutBottomMenu
            )
        },
        topBar = {
            Column {
                if (uiTopToolbarState is DateLayoutTopToolbarState.Content) {
                    DateLayoutTopToolbar(uiTopToolbarState, uiTopToolbarActions)
                }
                if (uiHeaderState is DateLayoutHeaderState.Content) {
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
                contentAlignment = Alignment.Center
            ) {
                if (uiHorizontalListState is DateLayoutHorizontalListState.Content) {
                    //DateLayoutHorizontalList(uiHorizontalListState, uiHorizontalListActions)
                }
                if (uiVerticalListState is DateLayoutVerticalListState.Content) {
                    //DateLayoutVerticalList(uiVerticalListState, uiVerticalListActions)
                }
            }
        }
    )
}

@Composable
private fun BottomMenu(
    uiDateLayoutBottomMenu: DateLayoutBottomMenu,
    uiBottomMenuActions: (DateLayoutBottomMenu.Action) -> Unit
) {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    if (isImeVisible) return
    BottomNavigationMenu(
        modifier = Modifier,
        backClick = { uiBottomMenuActions(DateLayoutBottomMenu.Action.Back) },
        backLongClick = { uiBottomMenuActions(DateLayoutBottomMenu.Action.BackLong) },
        searchClick  = { uiBottomMenuActions(DateLayoutBottomMenu.Action.GlobalSearch) },
        addDocClick = { uiBottomMenuActions(DateLayoutBottomMenu.Action.AddDoc) },
        addDocLongClick = { uiBottomMenuActions(DateLayoutBottomMenu.Action.CreateObjectLong) },
        isOwnerOrEditor = uiDateLayoutBottomMenu.isOwnerOrEditor
    )
}