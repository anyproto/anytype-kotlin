package com.anytypeio.anytype.feature_date.ui

import androidx.compose.runtime.Composable
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.feature_date.models.UiCalendarState
import com.anytypeio.anytype.feature_date.models.DateObjectBottomMenu
import com.anytypeio.anytype.feature_date.models.DateObjectHeaderState
import com.anytypeio.anytype.feature_date.models.DateObjectHorizontalListState
import com.anytypeio.anytype.feature_date.models.DateObjectSheetState
import com.anytypeio.anytype.feature_date.models.DateObjectTopToolbarState
import com.anytypeio.anytype.feature_date.models.DateObjectVerticalListState
import com.anytypeio.anytype.feature_date.models.UiContentState

//@DefaultPreviews
//@Composable
//fun PrevuewBottomSheet() {
//    DateObjectScreen(
//        uiTopToolbarState = DateObjectTopToolbarState.Content(
//            syncStatus = SpaceSyncStatus.SYNCING,
//        ),
//        uiHeaderState = DateObjectHeaderState.Content(
//            title = "06 Nov 2024"
//        ),
//        uiHorizontalListState = DateObjectHorizontalListState(
//            items = StubHorizontalItems,
//            selectedRelationKey = null
//        ),
//        uiVerticalListState = DateObjectVerticalListState(
//            items = StubVerticalItems
//        ),
//        uiDateObjectBottomMenu = DateObjectBottomMenu(isOwnerOrEditor = true),
//        uiSheetState = DateObjectSheetState.Content(
//            items = StubHorizontalItems
//        ),
//        uiHeaderActions = {},
//        uiTopToolbarActions = {},
//        uiHorizontalListActions = {},
//        uiVerticalListActions = {},
//        uiBottomMenuActions = {},
//        uiContentState = UiContentState.Idle(),
//        canPaginate = false,
//        onUpdateLimitSearch = {},
//        onCalendarDateSelected = {},
//        uiUiCalendarState = UiCalendarState.Calendar(
//            timeInMillis = 0L
//        )
//    )
//}