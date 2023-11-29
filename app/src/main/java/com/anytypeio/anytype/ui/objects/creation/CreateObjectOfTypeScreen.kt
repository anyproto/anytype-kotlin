package com.anytypeio.anytype.ui.objects.creation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.widgets.DragStates
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.objects.CreateObjectOfTypeViewModel
import com.anytypeio.anytype.presentation.objects.SelectTypeView
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun CreateObjectOfTypeScreen(
    vmCreateObject: CreateObjectOfTypeViewModel,
    onTypeClicked: (SelectTypeView.Type) -> Unit,
    onQueryChanged: (String) -> Unit,
    onFocused: () -> Unit,
    scope: CoroutineScope,
    useComposeIme: Boolean = false
) {

    val widgetState by vmCreateObject.viewVisibility.collectAsState()
    val widgetViews by vmCreateObject.views.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart,
    ) {
        val swipeableState = rememberSwipeableState(DragStates.VISIBLE)
        val keyboardController = LocalSoftwareKeyboardController.current

        AnimatedVisibility(
            visible = widgetState,
            enter = fadeIn(),
            exit = fadeOut(tween(DURATION_ANIMATION_OUT))
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .noRippleClickable { vmCreateObject.hideView() }
            )
        }

        if (swipeableState.isAnimationRunning && swipeableState.targetValue == DragStates.DISMISSED) {
            DisposableEffect(Unit) {
                onDispose {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    vmCreateObject.hideView()
                }
            }
        }

        if (!widgetState) {
            DisposableEffect(Unit) {
                onDispose {
                    scope.launch { swipeableState.snapTo(DragStates.VISIBLE) }
                }
            }
        }

        val sizePx =
            with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }

        val modifier = if (useComposeIme) {
            Modifier.imePadding()
        } else {
            Modifier
        }

        AnimatedVisibility(
            visible = widgetState,
            enter = slideInVertically { it },
            exit = slideOutVertically(tween(DURATION_ANIMATION_OUT)) { it },
            modifier = modifier
                .swipeable(state = swipeableState,
                    orientation = Orientation.Vertical,
                    anchors = mapOf(
                        0f to DragStates.VISIBLE, sizePx to DragStates.DISMISSED
                    ),
                    thresholds = { _, _ -> FractionalThreshold(0.3f) })
                .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                    .background(
                        color = colorResource(id = R.color.background_secondary),
                        shape = RoundedCornerShape(size = 16.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(bottom = 28.dp)
                ) {
                    Dragger(
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 6.dp)
                    )
                    SearchField(
                        onQueryChanged = onQueryChanged,
                        onFocused = onFocused,
                        focusRequester = focusRequester,
                        keyboardController = keyboardController
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRowContent(
                        views = widgetViews,
                        onTypeClicked = onTypeClicked
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun FlowRowContent(
    views: List<SelectTypeView>,
    onTypeClicked: (SelectTypeView.Type) -> Unit
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 12.dp)
            .verticalScroll(rememberScrollState())
        ,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        views.forEach { view ->
            when (view) {
                is SelectTypeView.Type -> {
                    ObjectTypeItem(
                        name = view.name,
                        emoji = view.icon,
                        onItemClicked = throttledClick(
                            onClick = { onTypeClicked(view) }
                        ),
                        modifier = Modifier
                    )
                }
                is SelectTypeView.Section.Groups -> {
                    Section(
                        title = stringResource(id = R.string.create_object_section_groups),
                    )
                }
                is SelectTypeView.Section.Objects -> {
                    Section(
                        title = stringResource(id = R.string.create_object_section_objects)
                    )
                }
                is SelectTypeView.Section.Library -> {
                    Section(
                        title = stringResource(id = R.string.create_object_section_library),
                    )
                }
            }

        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun LazyColumnContent(
    views: List<SelectTypeView>,
    onTypeClicked: (SelectTypeView.Type) -> Unit
) {
    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp
        )
    ) {
        views.forEach { view ->
            when (view) {
                is SelectTypeView.Section.Groups -> {
                    item(
                        key = view.javaClass.name,
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        Section(
                            title = stringResource(id = R.string.create_object_section_groups),
                        )
                    }
                }
                is SelectTypeView.Section.Objects -> {
                    item(
                        key = view.javaClass.name,
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        Section(
                            title = stringResource(id = R.string.create_object_section_objects)
                        )
                    }
                }
                is SelectTypeView.Section.Library -> {
                    item(
                        key = view.javaClass.name,
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        Section(
                            title = stringResource(id = R.string.create_object_section_library)
                        )
                    }
                }
                is SelectTypeView.Type -> {
                    item(
                        key = view.typeKey
                    ) {
                        ObjectTypeItem(
                            name = view.name,
                            emoji = view.icon,
                            onItemClicked = throttledClick(
                                onClick = {
                                    onTypeClicked(view)
                                }
                            ),
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun ObjectTypeItem(
    modifier: Modifier,
    name: String,
    emoji: String,
    onItemClicked: () -> Unit
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.shape_primary),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable { onItemClicked() }
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier.width(14.dp)
        )
        val uri = Emojifier.safeUri(emoji)
        if (uri.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(
                    Emojifier.safeUri(emoji)
                ),
                contentDescription = "Icon from URI",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = name,
            style = Title2,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun SearchField(
    onQueryChanged: (String) -> Unit,
    onFocused: () -> Unit,
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
    ) {
        val focusManager = LocalFocusManager.current
        val input = remember { mutableStateOf(String()) }
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color = colorResource(id = R.color.shape_transparent))
                .align(Alignment.Center)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_search_18),
                contentDescription = "Search icon",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
            )
            if (input.value.isNotEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_clear_18),
                    contentDescription = "Search icon",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                        .noRippleClickable {
                            input.value = ""
                            onQueryChanged("")
                        }
                )
            }
            BasicTextField(
                value = input.value,
                onValueChange = {
                    input.value = it
                    onQueryChanged(it)
                },
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 32.dp)
                    .align(Alignment.CenterStart)
                    .focusRequester(focusRequester)
                    .onFocusChanged { state ->
                        if (state.isFocused)
                            onFocused()
                    }
                ,
                maxLines = 1,
                singleLine = true,
                textStyle = BodyRegular.copy(
                    color = colorResource(id = R.color.text_primary)
                ),
                cursorBrush = SolidColor(
                    colorResource(id = R.color.cursor_color)
                ),
                decorationBox = @Composable { innerTextField ->
                    TextFieldDefaults.OutlinedTextFieldDecorationBox(
                        value = input.value,
                        innerTextField = innerTextField,
                        singleLine = true,
                        enabled = true,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.search),
                                style = BodyRegular
                            )
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = colorResource(id = R.color.text_primary),
                            backgroundColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            errorBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            placeholderColor = colorResource(id = R.color.text_tertiary)
                        ),
                        interactionSource = remember { MutableInteractionSource() },
                        visualTransformation = VisualTransformation.None,
                        contentPadding = PaddingValues(
                            start = 0.dp,
                            top = 0.dp,
                            end = 0.dp,
                            bottom = 0.dp
                        ),
                        border = {},
                    )
                }
            )
        }
    }
}

@Composable
private fun Section(title: String) {
    Box(modifier = Modifier
        .height(44.dp)
        .fillMaxWidth()) {
        Text(
            modifier = Modifier
                .align(Alignment.BottomStart),
            text = title,
            color = colorResource(id = R.color.text_secondary),
            style = Caption1Medium
        )
    }
}


private const val DURATION_ANIMATION_OUT = 50