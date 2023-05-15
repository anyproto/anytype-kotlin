package com.anytypeio.anytype.ui.settings.remote

import android.widget.LinearLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyCallout
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonWarning
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.widgets.CollectionActionWidget
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.presentation.widgets.collection.CollectionUiState
import com.anytypeio.anytype.presentation.widgets.collection.CollectionViewModel
import com.anytypeio.anytype.ui.widgets.collection.ListView
import com.anytypeio.anytype.ui.widgets.collection.SearchBar
import com.anytypeio.anytype.ui.widgets.collection.TopBar
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun RemoteStorageScreen(vm: CollectionViewModel) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val showFileAlert by vm.openFileDeleteAlert.collectAsStateWithLifecycle()

    uiState.fold(
        onSuccess = { result ->
            ContentDisplay(vm, result, showFileAlert)
        }
    )
}

@Composable
fun ContentDisplay(
    vm: CollectionViewModel,
    collectionUiState: CollectionUiState,
    showFileAlert: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Dragger(modifier = Modifier.padding(top = 6.dp))
            TopBar(vm, collectionUiState)
            SearchBar(vm, collectionUiState)
            ListView(vm, collectionUiState)
        }

        ActionWidget(vm, collectionUiState)

        if (showFileAlert) {
            FileDeleteAlertSheet(vm)
        }
    }
}

@Composable
fun ActionWidget(vm: CollectionViewModel, collectionUiState: CollectionUiState) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val targetOffset = screenHeight - 160.dp
    val offset by animateDpAsState(if (collectionUiState.showWidget) targetOffset else screenHeight)
    val localDensity = LocalDensity.current

    AnimatedVisibility(
        visible = collectionUiState.showWidget,
        enter = slideInVertically(initialOffsetY = { 300 }),
        exit = slideOutVertically(targetOffsetY = { 300 })
    ) {
        AndroidView(
            modifier = Modifier
                .offset(y = offset)
                .fillMaxWidth()
                .wrapContentHeight()
                .absolutePadding(16.dp, 0.dp, 16.dp, 48.dp),
            factory = { context ->
                CollectionActionWidget(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setCardBackgroundColor(context.getColor(R.color.background_secondary))
                    radius = with(localDensity) { 16.dp.toPx() }
                    cardElevation = with(localDensity) { 16.dp.toPx() }
                    useCompatPadding = false
                    actionListener = { action ->
                        vm.onActionWidgetClicked(action)
                    }
                }
            },
            update = { widget ->
                widget.bind(collectionUiState.objectActions)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileDeleteAlertSheet(vm: CollectionViewModel) {
    val scope = rememberCoroutineScope()
    val modalBottomSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = { vm.onFileDeleteAlertDismiss() },
        containerColor = colorResource(id = R.color.background_secondary),
        dragHandle = {},
        sheetState = modalBottomSheet
    ) {
        FileDeleteAlert(
            onCancelClick = {
                scope.launch { modalBottomSheet.hide() }.invokeOnCompletion {
                    if (!modalBottomSheet.isVisible) {
                        vm.onFileDeleteAlertDismiss()
                    }
                }
            },
            onDeleteClick = {
                vm.onDeletionFilesAccepted()
            }
        )
    }
}

@Composable
fun FileDeleteAlert(
    onCancelClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 10.dp),
        content = {
            Text(
                text = stringResource(id = R.string.file_delete_alert_title),
                style = HeadlineHeading,
                color = colorResource(id = R.color.text_primary)
            )
            Text(
                text = stringResource(id = R.string.file_delete_alert_subtitle),
                style = BodyCallout,
                color = colorResource(id = R.color.text_primary),
                modifier = Modifier.padding(top = 8.dp)
            )
            Row(
                modifier = Modifier.padding(top = 20.dp)
            ) {
                ButtonSecondary(
                    text = stringResource(id = R.string.file_delete_cancel),
                    onClick = onCancelClick,
                    size = ButtonSize.LargeSecondary,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(end = 6.dp)
                )
                ButtonWarning(
                    text = stringResource(id = R.string.file_delete_delete),
                    onClick = onDeleteClick,
                    size = ButtonSize.Large,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(start = 6.dp)
                )
            }
        }
    )
}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun PreviewFileAlertScreen() {
    FileDeleteAlert({ }, { })
}

