package com.anytypeio.anytype.ui.objects.creation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.EmptyState
import com.anytypeio.anytype.core_ui.foundation.GRADIENT_TYPE_RED
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.objects.SelectTypeView
import com.anytypeio.anytype.presentation.objects.SelectTypeViewState
import kotlinx.coroutines.delay

@Preview
@Composable
fun PreviewScreen() {
    SelectObjectTypeScreen(
        onTypeClicked = {},
        state = SelectTypeViewState.Loading,
        onQueryChanged = {},
        onFocused = {},
        onUnpinTypeClicked = {},
        onPinOnTopClicked = {}
    )
}

@Composable
fun SelectObjectTypeScreen(
    onTypeClicked: (SelectTypeView.Type) -> Unit,
    onUnpinTypeClicked: (SelectTypeView.Type) -> Unit,
    onPinOnTopClicked: (SelectTypeView.Type) -> Unit,
    onQueryChanged: (String) -> Unit,
    onFocused: () -> Unit,
    state: SelectTypeViewState
) {
    Column(
        modifier = Modifier.nestedScroll(rememberNestedScrollInteropConnection())

    ) {
        Dragger(
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 6.dp)
                .verticalScroll(rememberScrollState())
        )
        SearchField(
            onQueryChanged = onQueryChanged,
            onFocused = onFocused
        )
        Spacer(modifier = Modifier.height(8.dp))
        ScreenContent(
            state = state,
            onTypeClicked = onTypeClicked,
            onPinOnTopClicked = onPinOnTopClicked,
            onUnpinTypeClicked = onUnpinTypeClicked
        )
    }
}

@Composable
private fun ScreenContent(
    state: SelectTypeViewState,
    onTypeClicked: (SelectTypeView.Type) -> Unit,
    onUnpinTypeClicked: (SelectTypeView.Type) -> Unit,
    onPinOnTopClicked: (SelectTypeView.Type) -> Unit
) {
    when (state) {
        is SelectTypeViewState.Content -> {
            FlowRowContent(
                views = state.views,
                onTypeClicked = onTypeClicked,
                onPinOnTopClicked = onPinOnTopClicked,
                onUnpinTypeClicked = onUnpinTypeClicked
            )
        }
        SelectTypeViewState.Empty -> {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    EmptyState(
                        modifier = Modifier.align(Alignment.Center),
                        title = stringResource(id = R.string.nothing_found),
                        description = stringResource(id = R.string.nothing_found_object_types),
                        icon = AlertConfig.Icon(
                            gradient = GRADIENT_TYPE_RED,
                            icon = R.drawable.ic_alert_error
                        )
                    )
                }
            }
        }

        SelectTypeViewState.Loading -> {}
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun FlowRowContent(
    views: List<SelectTypeView>,
    onTypeClicked: (SelectTypeView.Type) -> Unit,
    onUnpinTypeClicked: (SelectTypeView.Type) -> Unit,
    onPinOnTopClicked: (SelectTypeView.Type) -> Unit
) {
    FlowRow(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        views.forEach { view ->
            when (view) {
                is SelectTypeView.Type -> {
                    val isMenuExpanded = remember {
                        mutableStateOf(false)
                    }
                    Box {
                        ObjectTypeItem(
                            name = view.name,
                            emoji = view.icon,
                            onItemClicked = throttledClick(
                                onClick = { onTypeClicked(view) }
                            ),
                            onItemLongClicked = {
                                isMenuExpanded.value = !isMenuExpanded.value
                            },
                            modifier = Modifier
                        )
                        if (view.isPinnable) {
                            DropdownMenu(
                                expanded = isMenuExpanded.value,
                                onDismissRequest = { isMenuExpanded.value = false },
                                offset = DpOffset(x = 0.dp, y = 6.dp)
                            ) {
                                if (!view.isPinned || !view.isFirstInSection) {
                                    DropdownMenuItem(
                                        onClick = {
                                            isMenuExpanded.value = false
                                            onPinOnTopClicked(view)
                                        }
                                    ) {
                                        Text(
                                            text = stringResource(R.string.any_object_creation_menu_pin_on_top),
                                            style = BodyRegular,
                                            color = colorResource(id = R.color.text_primary)
                                        )
                                    }
                                }
                                if (view.isPinned) {
                                    DropdownMenuItem(
                                        onClick = {
                                            isMenuExpanded.value = false
                                            onUnpinTypeClicked(view)
                                        }
                                    ) {
                                        Text(
                                            text = stringResource(R.string.any_object_creation_menu_unpin),
                                            style = BodyRegular,
                                            color = colorResource(id = R.color.text_primary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                is SelectTypeView.Section.Pinned -> {
                    Section(
                        title = stringResource(id = R.string.create_object_section_pinned),
                    )
                }
                is SelectTypeView.Section.Groups -> {
                    Section(
                        title = stringResource(id = R.string.create_object_section_lists),
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
                            title = stringResource(id = R.string.create_object_section_lists),
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
                is SelectTypeView.Section.Pinned -> {
                    item(
                        key = view.javaClass.name,
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        Section(
                            title = stringResource(id = R.string.create_object_section_pinned)
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
                            onItemLongClicked = {

                            },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }

        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ObjectTypeItem(
    modifier: Modifier,
    name: String,
    emoji: String,
    onItemClicked: () -> Unit,
    onItemLongClicked: () -> Unit
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
            .combinedClickable(
                onClick = {
                    onItemClicked()
                },
                onLongClick = {
                    onItemLongClicked()
                }
            ),
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SearchField(
    onQueryChanged: (String) -> Unit,
    onFocused: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        val focusManager = LocalFocusManager.current
        val focusRequester = FocusRequester()
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
                    onDone = { focusManager.clearFocus() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 32.dp)
                    .align(Alignment.CenterStart)
                    .focusRequester(focusRequester)
                    .onFocusChanged { state ->
                        if (state.isFocused)
                            onFocused()
                    },
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
                                text = stringResource(R.string.search_hint),
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
    Box(
        modifier = Modifier
            .height(44.dp)
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.BottomStart),
            text = title,
            color = colorResource(id = R.color.text_secondary),
            style = Caption1Medium
        )
    }
}