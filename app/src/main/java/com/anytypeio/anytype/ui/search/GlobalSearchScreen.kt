package com.anytypeio.anytype.ui.search

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_ui.foundation.AlertIcon
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutMedium
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.search.GlobalSearchItemView
import com.anytypeio.anytype.presentation.search.GlobalSearchViewModel
import com.anytypeio.anytype.ui.settings.typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun GlobalSearchScreen(
    modifier: Modifier = Modifier,
    state: GlobalSearchViewModel.ViewState,
    onQueryChanged: (String) -> Unit,
    onObjectClicked: (GlobalSearchItemView) -> Unit,
    onShowRelatedClicked: (GlobalSearchItemView) -> Unit = {},
    onClearRelatedClicked: () -> Unit = {},
    focusOnStart: Boolean = true
) {

    val selectionColors = TextSelectionColors(
        backgroundColor = colorResource(id = R.color.cursor_color).copy(
            alpha = 0.2f
        ),
        handleColor = colorResource(id = R.color.cursor_color),
    )

    var showLoading by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.isLoading) {
        if (state.isLoading && !showLoading) {
            delay(AVOID_FLICKERING_DELAY)
            showLoading = true
        } else if (!state.isLoading && showLoading) {
            delay(100)
            showLoading = false
        }
    }

    var query by remember { mutableStateOf(TextFieldValue()) }

    if (state is GlobalSearchViewModel.ViewState.Init) {
        query = TextFieldValue(
            text = state.query,
            selection = TextRange(start = 0, end = state.query.length)
        )
    }

    if (state is GlobalSearchViewModel.ViewState.RelatedInit) {
        query = TextFieldValue(
            text = state.query,
            selection = TextRange(start = 0, end = state.query.length)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection())
    ) {

        val interactionSource = remember { MutableInteractionSource() }
        val focus = LocalFocusManager.current
        val focusRequester = FocusRequester()

        Dragger(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.CenterHorizontally)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 10.dp,
                    start = 16.dp,
                    end = 16.dp
                )
                .background(
                    color = colorResource(id = R.color.shape_transparent),
                    shape = RoundedCornerShape(10.dp)
                )
                .height(40.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_search_18),
                contentDescription = "Search icon",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(
                        start = 11.dp
                    )
            )
            CompositionLocalProvider(value = LocalTextSelectionColors provides selectionColors) {

                BasicTextField(
                    value = query,
                    modifier = Modifier
                        .weight(1.0f)
                        .padding(start = 6.dp)
                        .align(Alignment.CenterVertically)
                        .focusRequester(focusRequester),
                    textStyle = BodyRegular.copy(
                        color = colorResource(id = R.color.text_primary)
                    ),
                    onValueChange = { input ->
                        query = input.also {
                            onQueryChanged(input.text)
                        }
                    },
                    singleLine = true,
                    maxLines = 1,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focus.clearFocus(true)
                        }
                    ),
                    decorationBox = @Composable { innerTextField ->
                        TextFieldDefaults.OutlinedTextFieldDecorationBox(
                            value = query.text,
                            innerTextField = innerTextField,
                            enabled = true,
                            singleLine = true,
                            visualTransformation = VisualTransformation.None,
                            interactionSource = interactionSource,
                            placeholder = {
                                Text(
                                    text = stringResource(id = R.string.search),
                                    style = BodyRegular.copy(
                                        color = colorResource(id = R.color.text_tertiary)
                                    )
                                )
                            },
                            colors = TextFieldDefaults.textFieldColors(
                                backgroundColor = Color.Transparent,
                                cursorColor = colorResource(id = R.color.cursor_color),
                            ),
                            border = {},
                            contentPadding = PaddingValues()
                        )
                    },
                    cursorBrush = SolidColor(colorResource(id = R.color.palette_system_blue)),
                )
            }
            Box(
                modifier = Modifier.size(16.dp)
            ) {
                if (showLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.fillMaxSize(),
                        color = colorResource(id = R.color.glyph_active),
                        strokeWidth = 2.dp
                    )
                }
            }
            Spacer(Modifier.width(9.dp))
            AnimatedVisibility(
                visible = query.text.isNotEmpty(),
                enter = fadeIn(tween(100)),
                exit = fadeOut(tween(100))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_clear_18),
                    contentDescription = "Clear icon",
                    modifier = Modifier
                        .padding(end = 9.dp)
                        .noRippleClickable {
                            query = TextFieldValue().also {
                                onQueryChanged("")
                            }
                        }
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxSize()
        ) {
            if (state is GlobalSearchViewModel.ViewState.Related) {
                stickyHeader {
                    RelatedHeader(title = state.target.title) {
                        onClearRelatedClicked()
                        query = TextFieldValue()
                    }
                }
            }
            if (state is GlobalSearchViewModel.ViewState.RelatedInit) {
                stickyHeader {
                    RelatedHeader(title = state.target.title) {
                        onClearRelatedClicked()
                        query = TextFieldValue()
                    }
                }
            }
            items(
                count = state.views.size,
                key = { idx ->
                    "global-search-item-${state.views[idx].id}"
                }
            ) { idx ->
                val item = state.views[idx]
                if (idx == 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                GlobalSearchItem(
                    globalSearchItemView = item,
                    onObjectClicked = {
                        focus.clearFocus(true)
                        onObjectClicked(it)
                    },
                    onShowRelatedClicked = {
                        onShowRelatedClicked(it).also {
                            query = TextFieldValue()
                        }
                    },
                    focusManager = focus
                )
                if (idx != state.views.lastIndex) {
                    Divider(paddingStart = 16.dp, paddingEnd = 16.dp)
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }

            }
            item {
                AnimatedVisibility(
                    modifier = Modifier.fillParentMaxSize(),
                    visible = state.isEmptyState(),
                    enter = fadeIn(animationSpec = tween(1000, 0)),
                    exit = fadeOut(animationSpec = tween(150, 0))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (state is GlobalSearchViewModel.ViewState.Related) {
                            Text(
                                text = stringResource(R.string.global_search_no_related_objects_found),
                                modifier = Modifier.align(Alignment.Center),
                                color = colorResource(id = R.color.text_primary),
                                style = BodyCalloutRegular
                            )
                        } else {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AlertIcon(R.drawable.ic_popup_duck_56)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(id = R.string.nothing_found),
                                    style = BodyCalloutMedium,
                                    color = colorResource(id = R.color.text_primary),
                                )
                                Text(
                                    text = stringResource(id = R.string.try_different_search_query),
                                    style = BodyCalloutRegular,
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    color = colorResource(id = R.color.text_primary),
                                )
                            }
                        }
                    }
                }
            }
        }
        LaunchedEffect(Unit) {
            if (focusOnStart) {
                focusRequester.requestFocus()
            }
        }
    }
}

@Composable
private fun RelatedHeader(
    title: String,
    onClearRelatedClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.global_search_related_to))
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(title)
                }
            },
            style = Caption1Regular,
            color = colorResource(id = R.color.text_secondary),
            modifier = Modifier
                .weight(1.0f)
                .padding(
                    start = 20.dp,
                    bottom = 8.dp
                ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(id = R.string.clear),
            style = Caption1Regular,
            color = colorResource(id = R.color.text_secondary),
            modifier = Modifier
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 8.dp
                )
                .clickable {
                    onClearRelatedClicked()
                }
        )
    }
    Divider(
        paddingStart = 20.dp,
        paddingEnd = 20.dp
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GlobalSearchItem(
    globalSearchItemView: GlobalSearchItemView,
    onObjectClicked: (GlobalSearchItemView) -> Unit,
    onShowRelatedClicked: (GlobalSearchItemView) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    onObjectClicked(globalSearchItemView)
                },
                onLongClick = {
                    if (globalSearchItemView.links.isNotEmpty() || globalSearchItemView.backlinks.isNotEmpty()) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        focusManager.clearFocus(true)
                        scope.launch {
                            delay(AVOID_DROPDOWN_FLICKERING_DELAY)
                            isMenuExpanded = true
                        }
                    }
                },
                enabled = true
            )
            .then(
                if (isMenuExpanded)
                    Modifier.background(
                        color = colorResource(id = R.color.shape_tertiary),
                        shape = RoundedCornerShape(10.dp)
                    )
                else
                    Modifier
            )
    ) {
        ListWidgetObjectIcon(
            icon = globalSearchItemView.icon,
            iconSize = 48.dp,
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    top = 12.dp,
                    bottom = 12.dp
                ),
            onTaskIconClicked = {
                // Do nothing
            }
        )
        Column(
            modifier = Modifier
                .padding(
                    start = 76.dp,
                    bottom = 12.dp,
                    top = 12.dp,
                    end = 16.dp
                )
                .align(Alignment.CenterStart)
        ) {
            DefaultTitleWithHighlights(
                text = globalSearchItemView.title,
                nameMeta = globalSearchItemView.nameMeta
            )
            when (val meta = globalSearchItemView.meta) {
                is GlobalSearchItemView.Meta.Block -> {
                    if (meta.highlights.isEmpty()) {
                        Text(
                            text = meta.snippet,
                            style = Relations2,
                            color = colorResource(id = R.color.text_primary)
                        )
                    } else {
                        DefaultMetaBlockWithHighlights(
                            text = meta.snippet,
                            highlights = meta.highlights
                        )
                    }
                }

                is GlobalSearchItemView.Meta.Default -> {
                    if (meta.highlights.isEmpty()) {
                        Text(
                            text = "${meta.name}: ${meta.value}",
                            style = Relations2,
                            color = colorResource(id = R.color.text_primary)
                        )
                    } else {
                        DefaultMetaRelationWithHighlights(
                            title = meta.name,
                            value = meta.value,
                            highlights = meta.highlights
                        )
                    }
                }

                is GlobalSearchItemView.Meta.Status -> {
                    DefaultMetaStatusRelation(
                        title = meta.name,
                        value = meta.value,
                        color = meta.color
                    )
                }

                is GlobalSearchItemView.Meta.Tag -> {
                    DefaultMetaTagRelation(
                        title = meta.name,
                        value = meta.value,
                        color = meta.color
                    )
                }

                is GlobalSearchItemView.Meta.None -> {
                    // Draw nothing.
                }
            }
            Text(
                text = globalSearchItemView.type,
                style = Relations2,
                color = colorResource(id = R.color.text_secondary)
            )
        }
        if (globalSearchItemView.links.isNotEmpty() || globalSearchItemView.backlinks.isNotEmpty()) {
            SmallRectangle(
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
        MaterialTheme(
            typography = typography,
            shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(16.dp)),
        ) {
            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = {
                    isMenuExpanded = false
                },
                offset = DpOffset(
                    x = 8.dp,
                    y = 8.dp
                )
            ) {
                DropdownMenuItem(
                    onClick = {
                        onShowRelatedClicked(globalSearchItemView)
                    }
                ) {
                    Text(text = "Show related objects")
                }
            }
        }
    }
}

@Composable
fun SmallRectangle(modifier: Modifier) {
    Box(
        modifier = modifier
            .size(6.dp, 28.dp)
            .background(
                shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp),
                color = colorResource(id = R.color.glyph_active)
            )
    )
}

@Composable
private fun DefaultMetaBlockWithHighlights(
    text: String,
    highlights: List<IntRange>
) {
    Text(
        text = AnnotatedString(
            text = text,
            spanStyles = highlights.map { range ->
                AnnotatedString.Range(
                    SpanStyle(
                        background = colorResource(id = R.color.palette_light_ice)
                    ),
                    range.first,
                    range.last
                )
            }
        ),
        style = Relations2,
        color = colorResource(id = R.color.text_primary)
    )
}

@Composable
private fun DefaultTitleWithHighlights(
    text: String,
    nameMeta: GlobalSearchItemView.NameMeta?
) {
    val title = when {
        text.isEmpty() -> AnnotatedString(stringResource(id = R.string.untitled))
        nameMeta == null -> AnnotatedString(text)
        else -> AnnotatedString(
            text = text,
            spanStyles = nameMeta.highlights.map { range ->
                AnnotatedString.Range(
                    SpanStyle(
                        background = colorResource(id = R.color.palette_light_ice)
                    ),
                    range.first,
                    range.last
                )
            }
        )
    }
    Text(
        text = title,
        style = PreviewTitle2Medium,
        color = colorResource(id = R.color.text_primary),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun DefaultMetaRelationWithHighlights(
    title: String,
    value: String,
    highlights: List<IntRange>
) {
    Text(
        text = buildAnnotatedString {
            append("${title}: ")
            append(
                AnnotatedString(
                    text = value,
                    spanStyles = highlights.map { range ->
                        AnnotatedString.Range(
                            SpanStyle(
                                background = colorResource(id = R.color.palette_light_ice)
                            ),
                            range.first,
                            range.last
                        )
                    }
                )
            )
        },
        style = Relations2,
        color = colorResource(id = R.color.text_primary)
    )
}

@Composable
private fun DefaultMetaTagRelation(
    title: String,
    value: String,
    color: ThemeColor
) {
    Row {
        Text(
            text = "${title}: ",
            style = Relations2,
            color = colorResource(id = R.color.text_primary)
        )
        Text(
            text = value,
            style = Relations2,
            color = dark(color),
            modifier = Modifier
                .background(
                    color = light(color = color),
                    shape = RoundedCornerShape(size = 3.dp)
                )
                .padding(start = 6.dp, end = 6.dp),
        )
    }
}

@Composable
private fun DefaultMetaStatusRelation(
    title: String,
    value: String,
    color: ThemeColor
) {
    Text(
        text = buildAnnotatedString {
            append("${title}: ")
            append(
                AnnotatedString(
                    text = value,
                    spanStyles = listOf(
                        AnnotatedString.Range(
                            SpanStyle(color = dark(color = color)),
                            0,
                            value.length
                        )
                    )
                )
            )
        },
        style = Relations2,
        color = colorResource(id = R.color.text_primary)
    )
}

fun dummyObject() = ObjectWrapper.Basic(
    mapOf(
        "id" to "1",
        "name" to "Name",
        "description" to "Description11",
        Relations.SPACE_ID to "1",
        Relations.LAYOUT to ObjectType.Layout.BASIC.code
    )
)

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
private fun DefaultGlobalSearchItemViewPreview() {
    GlobalSearchItem(
        GlobalSearchItemView(
            id = "ID",
            space = SpaceId(""),
            title = "Autechre",
            type = "Band",
            meta = GlobalSearchItemView.Meta.None,
            layout = ObjectType.Layout.BASIC,
            icon = ObjectIcon.Basic.Emoji("🎵"),
            obj = dummyObject()
        ),
        onObjectClicked = {},
        onShowRelatedClicked = {},
        focusManager = LocalFocusManager.current
    )
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
private fun DefaultGlobalSearchItemViewWithLongTitlePreview() {
    GlobalSearchItem(
        GlobalSearchItemView(
            id = "ID",
            space = SpaceId(""),
            title = "AutechreAutechreAutechreAutechreAutechreAutechreAutechre",
            type = "Band",
            meta = GlobalSearchItemView.Meta.None,
            layout = ObjectType.Layout.BASIC,
            icon = ObjectIcon.Basic.Emoji("🎵"),
            obj = dummyObject()
        ),
        onObjectClicked = {},
        onShowRelatedClicked = {},
        focusManager = LocalFocusManager.current
    )
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
private fun DefaultGlobalSearchItemViewWithBlockMetaPreview() {
    GlobalSearchItem(
        GlobalSearchItemView(
            id = "ID",
            space = SpaceId(""),
            title = "Autechre",
            type = "Band",
            meta = GlobalSearchItemView.Meta.Block(
                snippet = "Autechre are an English electronic music duo consisting of Rob Brown and Sean Booth, both from Rochdale, Greater Manchester. ",
                highlights = emptyList()
            ),
            layout = ObjectType.Layout.BASIC,
            icon = ObjectIcon.Basic.Emoji("🎵"),
            obj = dummyObject()
        ),
        onObjectClicked = {},
        onShowRelatedClicked = {},
        focusManager = LocalFocusManager.current
    )
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
private fun DefaultGlobalSearchItemViewBlockTwoHighlightsMetaPreview() {
    GlobalSearchItem(
        GlobalSearchItemView(
            id = "ID",
            space = SpaceId(""),
            title = "Autechre",
            type = "Band",
            meta = GlobalSearchItemView.Meta.Block(
                snippet = "Autechre are an English electronic music duo consisting of Rob Brown and Sean Booth, both from Rochdale, Greater Manchester. ",
                highlights = listOf(
                    IntRange(0, 8),
                    IntRange(15, 23)
                )
            ),
            layout = ObjectType.Layout.BASIC,
            icon = ObjectIcon.Basic.Emoji("🎵"),
            obj = dummyObject()
        ),
        onObjectClicked = {},
        onShowRelatedClicked = {},
        focusManager = LocalFocusManager.current
    )
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
private fun DefaultGlobalSearchItemViewRelationTwoHighlightsMetaPreview() {
    GlobalSearchItem(
        GlobalSearchItemView(
            id = "ID",
            space = SpaceId(""),
            title = "Autechre",
            type = "Band",
            meta = GlobalSearchItemView.Meta.Default(
                name = "Description",
                value = "Autechre are an English electronic music duo consisting of Rob Brown and Sean Booth, both from Rochdale, Greater Manchester. ",
                highlights = listOf(
                    IntRange(0, 8),
                    IntRange(15, 23)
                )
            ),
            layout = ObjectType.Layout.BASIC,
            icon = ObjectIcon.Basic.Emoji("🎵"),
            obj = dummyObject()
        ),
        onObjectClicked = {},
        onShowRelatedClicked = {},
        focusManager = LocalFocusManager.current
    )
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
private fun DefaultGlobalSearchItemViewTagRelationPreview() {
    GlobalSearchItem(
        GlobalSearchItemView(
            id = "ID",
            space = SpaceId(""),
            title = "Autechre",
            type = "Band",
            meta = GlobalSearchItemView.Meta.Tag(
                name = "Style",
                value = "IDM",
                color = ThemeColor.TEAL
            ),
            layout = ObjectType.Layout.BASIC,
            icon = ObjectIcon.Basic.Emoji("🎵"),
            obj = dummyObject()
        ),
        onObjectClicked = {},
        onShowRelatedClicked = {},
        focusManager = LocalFocusManager.current
    )
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
private fun DefaultGlobalSearchItemViewStatusRelationPreview() {
    GlobalSearchItem(
        GlobalSearchItemView(
            id = "ID",
            space = SpaceId(""),
            title = "Autechre",
            type = "Band",
            meta = GlobalSearchItemView.Meta.Status(
                name = "Style",
                value = "IDM",
                color = ThemeColor.TEAL
            ),
            layout = ObjectType.Layout.BASIC,
            icon = ObjectIcon.Basic.Emoji("🎵"),
            obj = dummyObject()
        ),
        onObjectClicked = {},
        onShowRelatedClicked = {},
        focusManager = LocalFocusManager.current
    )
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
private fun DefaultGlobalSearchItemViewStatusRelationScreenPreview() {

    GlobalSearchScreen(
        onQueryChanged = {},
        state = GlobalSearchViewModel.ViewState.Default(
            isLoading = false,
            views = listOf(
                GlobalSearchItemView(
                    id = "ID1",
                    space = SpaceId(""),
                    title = "Autechre",
                    type = "Band",
                    meta = GlobalSearchItemView.Meta.None,
                    layout = ObjectType.Layout.BASIC,
                    icon = ObjectIcon.Basic.Emoji("🎵"),
                    obj = dummyObject()
                ),
                GlobalSearchItemView(
                    id = "ID2",
                    space = SpaceId(""),
                    title = "Autechre",
                    type = "Band",
                    meta = GlobalSearchItemView.Meta.Status(
                        name = "Style",
                        value = "IDM",
                        color = ThemeColor.TEAL
                    ),
                    layout = ObjectType.Layout.BASIC,
                    icon = ObjectIcon.Basic.Emoji("🎵"),
                    obj = dummyObject()
                ),
                GlobalSearchItemView(
                    id = "ID3",
                    space = SpaceId(""),
                    title = "Autechre",
                    type = "Band",
                    meta = GlobalSearchItemView.Meta.Tag(
                        name = "Style",
                        value = "IDM",
                        color = ThemeColor.TEAL
                    ),
                    layout = ObjectType.Layout.BASIC,
                    icon = ObjectIcon.Basic.Emoji("🎵"),
                    obj = dummyObject()
                ),
                GlobalSearchItemView(
                    id = "ID4",
                    space = SpaceId(""),
                    title = "Autechre",
                    type = "Band",
                    meta = GlobalSearchItemView.Meta.Default(
                        name = "Description",
                        value = "Autechre are an English electronic music duo consisting of Rob Brown and Sean Booth, both from Rochdale, Greater Manchester. ",
                        highlights = listOf(
                            IntRange(0, 8),
                            IntRange(15, 23)
                        )
                    ),
                    layout = ObjectType.Layout.BASIC,
                    icon = ObjectIcon.Basic.Emoji("🎵"),
                    obj = dummyObject()
                ),
                GlobalSearchItemView(
                    id = "ID5",
                    space = SpaceId(""),
                    title = "Autechre",
                    type = "Band",
                    meta = GlobalSearchItemView.Meta.Block(
                        snippet = "Autechre are an English electronic music duo consisting of Rob Brown and Sean Booth, both from Rochdale, Greater Manchester. ",
                        highlights = emptyList()
                    ),
                    layout = ObjectType.Layout.BASIC,
                    icon = ObjectIcon.Basic.Emoji("🎵"),
                    obj = dummyObject()
                )
            )
        ),
        onObjectClicked = {},
        onShowRelatedClicked = {},
        onClearRelatedClicked = {}
    )
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
private fun DefaultGlobalSearchItemViewWithRelatedScreenPreview() {
    GlobalSearchScreen(
        onQueryChanged = {},
        state = GlobalSearchViewModel.ViewState.Related(
            isLoading = false,
            target = GlobalSearchItemView(
                id = "ID1",
                space = SpaceId(""),
                title = "Autechre Autechre Autechre Autechre Autechre Autechre Autechre Autechre",
                type = "Band",
                meta = GlobalSearchItemView.Meta.None,
                layout = ObjectType.Layout.BASIC,
                icon = ObjectIcon.Basic.Emoji("🎵"),
                obj = dummyObject()
            ),
            views = listOf(
                GlobalSearchItemView(
                    id = "ID1",
                    space = SpaceId(""),
                    title = "Autechre",
                    type = "Band",
                    meta = GlobalSearchItemView.Meta.None,
                    layout = ObjectType.Layout.BASIC,
                    icon = ObjectIcon.Basic.Emoji("🎵"),
                    obj = dummyObject()
                ),
                GlobalSearchItemView(
                    id = "ID2",
                    space = SpaceId(""),
                    title = "Autechre",
                    type = "Band",
                    meta = GlobalSearchItemView.Meta.Status(
                        name = "Style",
                        value = "IDM",
                        color = ThemeColor.TEAL
                    ),
                    layout = ObjectType.Layout.BASIC,
                    icon = ObjectIcon.Basic.Emoji("🎵"),
                    obj = dummyObject()
                ),
                GlobalSearchItemView(
                    id = "ID3",
                    space = SpaceId(""),
                    title = "Autechre",
                    type = "Band",
                    meta = GlobalSearchItemView.Meta.Tag(
                        name = "Style",
                        value = "IDM",
                        color = ThemeColor.TEAL
                    ),
                    layout = ObjectType.Layout.BASIC,
                    icon = ObjectIcon.Basic.Emoji("🎵"),
                    obj = dummyObject()
                ),
                GlobalSearchItemView(
                    id = "ID4",
                    space = SpaceId(""),
                    title = "Autechre",
                    type = "Band",
                    meta = GlobalSearchItemView.Meta.Default(
                        name = "Description",
                        value = "Autechre are an English electronic music duo consisting of Rob Brown and Sean Booth, both from Rochdale, Greater Manchester. ",
                        highlights = listOf(
                            IntRange(0, 8),
                            IntRange(15, 23)
                        )
                    ),
                    layout = ObjectType.Layout.BASIC,
                    icon = ObjectIcon.Basic.Emoji("🎵"),
                    obj = dummyObject()
                ),
                GlobalSearchItemView(
                    id = "ID5",
                    space = SpaceId(""),
                    title = "Autechre",
                    type = "Band",
                    meta = GlobalSearchItemView.Meta.Block(
                        snippet = "Autechre are an English electronic music duo consisting of Rob Brown and Sean Booth, both from Rochdale, Greater Manchester. ",
                        highlights = emptyList()
                    ),
                    layout = ObjectType.Layout.BASIC,
                    icon = ObjectIcon.Basic.Emoji("🎵"),
                    obj = dummyObject()
                )
            )
        ),
        onObjectClicked = {},
        onShowRelatedClicked = {},
        onClearRelatedClicked = {}
    )
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
private fun DefaultGlobalSearchEmptyStatePreview() {
    GlobalSearchScreen(
        onQueryChanged = {},
        state = GlobalSearchViewModel.ViewState.Default(
            isLoading = false,
            views = emptyList()
        ),
        onObjectClicked = {},
        onShowRelatedClicked = {},
        onClearRelatedClicked = {}
    )
}

const val AVOID_FLICKERING_DELAY = 100L
const val AVOID_DROPDOWN_FLICKERING_DELAY = 50L