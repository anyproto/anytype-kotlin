package com.anytypeio.anytype.ui.settings.remote

import android.widget.LinearLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Text
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyCallout
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonWarning
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.widgets.CollectionActionWidget
import com.anytypeio.anytype.core_ui.widgets.DragStates
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.presentation.widgets.collection.CollectionUiState
import com.anytypeio.anytype.presentation.widgets.collection.CollectionViewModel
import com.anytypeio.anytype.ui.widgets.collection.ListView
import com.anytypeio.anytype.ui.widgets.collection.SearchBar
import com.anytypeio.anytype.ui.widgets.collection.TopBar
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun RemoteFilesManageScreen(vm: CollectionViewModel, scope: CoroutineScope) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val showFileAlert by vm.openFileDeleteAlert.collectAsStateWithLifecycle()

    uiState.fold(
        onSuccess = { result ->
            ContentDisplay(vm, result, showFileAlert, scope)
        }
    )
}

@Composable
fun ContentDisplay(
    vm: CollectionViewModel,
    collectionUiState: CollectionUiState,
    showFileAlert: Boolean,
    scope: CoroutineScope
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .nestedScroll(rememberNestedScrollInteropConnection())
            .background(color = colorResource(id = R.color.background_primary)),
        contentAlignment = Alignment.BottomStart
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Dragger(modifier = Modifier.padding(top = 6.dp))
            TopBar(vm, collectionUiState)
            SearchBar(vm, collectionUiState)
            ListView(
                vm = vm,
                uiState = collectionUiState
            )
        }

        ActionWidget(vm, collectionUiState)

        FileDeleteAlertSheet(showFileAlert, vm, scope)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ActionWidget(vm: CollectionViewModel, collectionUiState: CollectionUiState) {

    val localDensity = LocalDensity.current

    val swipeableState = rememberSwipeableState(DragStates.VISIBLE)

    AnimatedVisibility(
        visible = collectionUiState.showWidget,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = Modifier
            .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
    ) {
        AndroidView(
            modifier = Modifier
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FileDeleteAlertSheet(
    showFileAlert: Boolean,
    vm: CollectionViewModel,
    scope: CoroutineScope
) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart,
    ) {

        val swipeableState = rememberSwipeableState(DragStates.VISIBLE)

        val sizePx =
            with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }

        AnimatedVisibility(
            visible = showFileAlert,
            enter = fadeIn(),
            exit = fadeOut(tween(100))
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .noRippleClickable { vm.onFileDeleteAlertDismiss() }
            )
        }

        if (swipeableState.isAnimationRunning && swipeableState.targetValue == DragStates.DISMISSED) {
            DisposableEffect(Unit) {
                onDispose {
                    vm.onFileDeleteAlertDismiss()
                }
            }
        }

        if (!showFileAlert) {
            DisposableEffect(Unit) {
                onDispose {
                    scope.launch { swipeableState.snapTo(DragStates.VISIBLE) }
                }
            }
        }

        AnimatedVisibility(
            visible = showFileAlert,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier
                .swipeable(
                    state = swipeableState,
                    orientation = Orientation.Vertical,
                    anchors = mapOf(
                        0f to DragStates.VISIBLE, sizePx to DragStates.DISMISSED
                    ),
                    thresholds = { _, _ -> FractionalThreshold(0.3f) })
                .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
        ) {
            FileDeleteAlert(
                onCancelClick = {
                    vm.onFileDeleteAlertDismiss()
                },
                onDeleteClick = {
                    vm.onDeletionFilesAccepted()
                }
            )
        }

    }
}

@Composable
fun FileDeleteAlert(
    onCancelClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 8.dp, end = 8.dp, bottom = 42.dp)
            .background(
                color = colorResource(id = R.color.background_secondary),
                shape = RoundedCornerShape(size = 16.dp)
            )
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
}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun PreviewFileAlertScreen() {
    FileDeleteAlert({ }, { })
}

