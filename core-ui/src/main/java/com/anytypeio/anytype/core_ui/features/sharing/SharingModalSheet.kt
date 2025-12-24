package com.anytypeio.anytype.core_ui.features.sharing

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.sharing.SelectableObjectView
import com.anytypeio.anytype.presentation.sharing.SelectableSpaceView
import com.anytypeio.anytype.presentation.sharing.SharingScreenState
import kotlinx.coroutines.launch

/**
 * Pure Compose ModalBottomSheet wrapper for the sharing extension screen.
 *
 * This composable replaces the legacy [BaseBottomSheetComposeFragment] approach with
 * a modern Material3 ModalBottomSheet, providing:
 * - Better state management (single source of truth)
 * - Simpler lifecycle (no Fragment complexity)
 * - Modern animations and gestures
 * - Easier testing with Compose testing framework
 *
 * @param state The current screen state from SharingViewModel
 * @param onSpaceSelected Callback when a space is selected in the grid
 * @param onSearchQueryChanged Callback when search query changes
 * @param onCommentChanged Callback when comment text changes
 * @param onSendClicked Callback when send button is clicked
 * @param onObjectSelected Callback when an object is selected in destination list
 * @param onBackPressed Callback when back navigation is triggered
 * @param onDismiss Callback when the sheet is dismissed (swipe down or back press)
 * @param onRetryClicked Callback when retry button is clicked on error screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharingModalSheet(
    state: SharingScreenState,
    onSpaceSelected: (SelectableSpaceView) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onCommentChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onObjectSelected: (SelectableObjectView) -> Unit,
    onBackPressed: () -> Unit,
    onDismiss: () -> Unit,
    onRetryClicked: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        containerColor = colorResource(R.color.background_primary),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null
    ) {
        SharingScreen(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onSpaceSelected = onSpaceSelected,
            onSearchQueryChanged = onSearchQueryChanged,
            onCommentChanged = onCommentChanged,
            onSendClicked = onSendClicked,
            onObjectSelected = onObjectSelected,
            onBackPressed = { onBackPressed() },
            onCancelClicked = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        onDismiss()
                    }
                }
            },
            onRetryClicked = onRetryClicked
        )
    }
}
