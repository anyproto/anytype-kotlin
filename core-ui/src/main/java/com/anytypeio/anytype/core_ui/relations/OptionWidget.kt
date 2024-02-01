package com.anytypeio.anytype.core_ui.relations

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Text
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.widgets.DragStates
import com.anytypeio.anytype.presentation.relations.value.tagstatus.OptionWidgetAction
import com.anytypeio.anytype.presentation.relations.value.tagstatus.OptionWidgetViewState
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun OptionWidget(
    state: OptionWidgetViewState,
    action: (OptionWidgetAction) -> Unit,
    scope: CoroutineScope
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val isVisible = true

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart,
    ) {

        val currentState by rememberUpdatedState(state)
        val swipeableState = rememberSwipeableState(DragStates.VISIBLE)
        val keyboardController = LocalSoftwareKeyboardController.current

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut(tween(100))
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .noRippleClickable {
                        //action(OptionWidgetAction.Dismiss)
                    }
            )
        }

        if (swipeableState.isAnimationRunning && swipeableState.targetValue == DragStates.DISMISSED) {
            DisposableEffect(Unit) {
                onDispose {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    //action(OptionWidgetAction.Dismiss)
                }
            }
        }

        if (!isVisible) {
            DisposableEffect(Unit) {
                onDispose {
                    scope.launch { swipeableState.snapTo(DragStates.VISIBLE) }
                }
            }
        }

        val sizePx =
            with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }

        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier
                .swipeable(state = swipeableState,
                    orientation = Orientation.Vertical,
                    anchors = mapOf(
                        0f to DragStates.VISIBLE, sizePx to DragStates.DISMISSED
                    ),
                    thresholds = { _, _ -> FractionalThreshold(0.3f) })
                .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
        ) {
            if (isVisible) {
                OptionWidgetContent(
                    state = currentState as OptionWidgetViewState,
                    action = action,
                    focusRequester = focusRequester,
                    keyboardController = keyboardController
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OptionWidgetContent(
    state: OptionWidgetViewState,
    action: (OptionWidgetAction) -> Unit,
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(bottom = 20.dp)
    ) {
        Header(state = state, action = action)
        TextInput(
            state = state,
            action = action,
            focusRequester = focusRequester,
            keyboardController = keyboardController
        )
        Divider(paddingEnd = 0.dp, paddingStart = 0.dp)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TextInput(
    state: OptionWidgetViewState,
    action: (OptionWidgetAction) -> Unit,
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?
) {
    var innerValue by remember(state) { mutableStateOf(state.text) }
    val focusManager = LocalFocusManager.current

    if (state.text.isEmpty()) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }

    BasicTextField(
        value = innerValue,
        onValueChange = { innerValue = it },
        textStyle = Title1.copy(color = colorResource(id = R.color.text_primary)),
        singleLine = true,
        enabled = true,
        cursorBrush = SolidColor(colorResource(id = R.color.text_primary)),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 0.dp, top = 2.dp)
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions {
            keyboardController?.hide()
            focusManager.clearFocus()
            //action.invoke()
        },
        decorationBox = { innerTextField ->
            if (innerValue.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.new_view),
                    style = Title1,
                    color = colorResource(id = R.color.text_tertiary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                )
            }
            innerTextField()
        }
    )
}

@Composable
private fun Header(state: OptionWidgetViewState, action: (OptionWidgetAction) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Dragger(modifier = Modifier.align(Alignment.Center))
    }
    val text = when (state) {
        is OptionWidgetViewState.Create -> stringResource(id = R.string.option_widget_create)
        is OptionWidgetViewState.Edit -> stringResource(id = R.string.option_widget_edit)
    }

    // Main content box
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 74.dp),
            text = text,
            style = Title1.copy(),
            color = colorResource(R.color.text_primary),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, device = Devices.PIXEL_4_XL)
@Composable
fun PreviewOptionWidget() {
    OptionWidget(
        state = OptionWidgetViewState.Create(
            optionId = "1",
            text = "Urgent",
            color = ThemeColor.BLUE,
        ),
        action = {},
        scope = CoroutineScope(Dispatchers.Main)
    )
}
