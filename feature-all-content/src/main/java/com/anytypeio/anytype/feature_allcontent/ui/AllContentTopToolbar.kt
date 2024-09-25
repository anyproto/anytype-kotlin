package com.anytypeio.anytype.feature_allcontent.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.feature_allcontent.R
import com.anytypeio.anytype.feature_allcontent.models.AllContentTab
import com.anytypeio.anytype.feature_allcontent.models.MenuButtonViewState
import com.anytypeio.anytype.feature_allcontent.models.TabsViewState
import com.anytypeio.anytype.feature_allcontent.models.TopBarViewState

//region AllContentTopBarContainer

@Composable
fun AllContentTopBarContainer(state: TopBarViewState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        AllContentTitle(state = state.titleState)
        AllContentMenuButton(state = state.menuButtonState)
    }
}

@DefaultPreviews
@Composable
private fun AllContentTopBarContainerPreview() {
    AllContentTopBarContainer(
        state = TopBarViewState(
            titleState = AllContentTitleViewState.OnlyUnlinked,
            menuButtonState = MenuButtonViewState.Visible
        )
    )
}
//endregion

//region AllContentTitle
sealed class AllContentTitleViewState {
    data object Hidden : AllContentTitleViewState()
    data object AllContent : AllContentTitleViewState()
    data object OnlyUnlinked : AllContentTitleViewState()
}

@Composable
fun BoxScope.AllContentTitle(state: AllContentTitleViewState) {
    when (state) {
        AllContentTitleViewState.Hidden -> return
        AllContentTitleViewState.AllContent -> {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Center),
                text = stringResource(id = R.string.all_content_title_all_content),
                style = Title1,
                color = colorResource(id = R.color.text_primary)
            )
        }

        AllContentTitleViewState.OnlyUnlinked -> {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Center),
                text = stringResource(id = R.string.all_content_title_only_unlinked),
                style = Title1,
                color = colorResource(id = R.color.text_primary)
            )
        }
    }
}
//endregion

//region AllContentMenuButton
@Composable
fun BoxScope.AllContentMenuButton(state: MenuButtonViewState) {
    when (state) {
        MenuButtonViewState.Hidden -> return
        MenuButtonViewState.Visible -> {
            Image(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(32.dp)
                    .align(Alignment.CenterEnd),
                painter = painterResource(id = R.drawable.ic_space_list_dots),
                contentDescription = "Menu icon",
                contentScale = ContentScale.Inside
            )
        }
    }
}
//endregion

//region AllContentTabs
@Composable
fun AllContentTabs(state: TabsViewState, onClick: () -> Unit) {
    val scrollState = rememberLazyListState()
    var selectedTab by remember { mutableStateOf<AllContentTab?>(null) }

    when (state) {
        is TabsViewState.Hidden -> return
        is TabsViewState.Visible -> {
            LaunchedEffect(onClick) {
                scrollState.animateScrollToItem(state.tabs.indexOf(selectedTab))
            }
            val snapFlingBehavior = rememberSnapFlingBehavior(scrollState)
            LazyRow(
                state = scrollState,
                flingBehavior = snapFlingBehavior,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp)
            ) {
                items(
                    count = state.tabs.size,
                    key = { index -> state.tabs[index].ordinal },
                ) { index ->
                    val tab = state.tabs[index]
                    AllContentTabText(
                        tab = tab,
                        isSelected = tab == selectedTab,
                        onClick = {
                            selectedTab = tab
                            onClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AllContentTabText(
    tab: AllContentTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Text(
        modifier = Modifier
            .wrapContentSize()
            .clickable { onClick() },
        text = getTabText(tab),
        style = Title2,
        color = if (isSelected) colorResource(id = R.color.glyph_button) else colorResource(id = R.color.glyph_active),
        maxLines = 1
    )
}

@Composable
private fun getTabText(tab: AllContentTab): String {
    return when (tab) {
        AllContentTab.OBJECTS -> stringResource(id = R.string.all_content_title_tab_objetcs)
        AllContentTab.FILES -> stringResource(id = R.string.all_content_title_tab_files)
        AllContentTab.MEDIA -> stringResource(id = R.string.all_content_title_tab_media)
        AllContentTab.BOOKMARKS -> stringResource(id = R.string.all_content_title_tab_bookmarks)
        AllContentTab.TYPES -> stringResource(id = R.string.all_content_title_tab_objetc_types)
        AllContentTab.RELATIONS -> stringResource(id = R.string.all_content_title_tab_relations)
    }
}

@DefaultPreviews
@Composable
private fun AllContentTabsPreview() {
    AllContentTabs(
        state = TabsViewState.Visible(
            tabs = listOf(
                AllContentTab.OBJECTS,
                AllContentTab.FILES,
                AllContentTab.MEDIA,
                AllContentTab.BOOKMARKS,
                AllContentTab.TYPES,
                AllContentTab.RELATIONS
            )
        ),
        onClick = {}
    )
}

//endregion
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AllContentSearchBar() {

    val interactionSource = remember { MutableInteractionSource() }
    val focus = LocalFocusManager.current
    val focusRequester = FocusRequester()

    val selectionColors = TextSelectionColors(
        backgroundColor = colorResource(id = R.color.cursor_color).copy(
            alpha = 0.2f
        ),
        handleColor = colorResource(id = R.color.cursor_color),
    )

    var query by remember { mutableStateOf(TextFieldValue()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
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
                        //onQueryChanged(input.text)
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
                            backgroundColor = colorResource(id = R.color.shape_transparent),
                            cursorColor = colorResource(id = R.color.cursor_color),
                        ),
                        border = {},
                        contentPadding = PaddingValues()
                    )
                },
                cursorBrush = SolidColor(colorResource(id = R.color.palette_system_blue)),
            )
        }
//        Box(
//            modifier = Modifier.size(16.dp)
//        ) {
//            if (showLoading) {
//                CircularProgressIndicator(
//                    modifier = Modifier.fillMaxSize(),
//                    color = colorResource(id = R.color.glyph_active),
//                    strokeWidth = 2.dp
//                )
//            }
//        }
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
                            //onQueryChanged("")
                        }
                    }
            )
        }
    }
}

@DefaultPreviews
@Composable
private fun AllContentSearchBarPreview() {
    AllContentSearchBar()
}
//region SearchBar

//endregion