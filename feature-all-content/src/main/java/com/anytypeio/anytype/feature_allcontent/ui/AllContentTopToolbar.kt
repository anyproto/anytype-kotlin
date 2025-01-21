package com.anytypeio.anytype.feature_allcontent.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.bouncingClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.feature_allcontent.R
import com.anytypeio.anytype.feature_allcontent.models.AllContentMenuMode
import com.anytypeio.anytype.feature_allcontent.models.AllContentTab
import com.anytypeio.anytype.feature_allcontent.models.UiMenuState
import com.anytypeio.anytype.feature_allcontent.models.UiTabsState
import com.anytypeio.anytype.feature_allcontent.models.UiTitleState
import com.anytypeio.anytype.presentation.objects.MenuSortsItem
import com.anytypeio.anytype.presentation.objects.ObjectsListSort

//region AllContentTopBarContainer
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllContentTopBarContainer(
    titleState: UiTitleState,
    uiMenuState: UiMenuState,
    onModeClick: (AllContentMenuMode) -> Unit,
    onSortClick: (ObjectsListSort) -> Unit,
    onBinClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        modifier = Modifier.fillMaxWidth(),
        expandedHeight = 48.dp,
        title = { AllContentTitle(state = titleState) },
        navigationIcon = {
            Image(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .noRippleClickable {
                        onBackClick()
                    }
                ,
                painter = painterResource(R.drawable.ic_default_top_back),
                contentDescription = stringResource(R.string.content_desc_back_button)
            )
        },
        actions = {
            AllContentMenuButton(
                onClick = { isMenuExpanded = true }
            )
            if (uiMenuState is UiMenuState.Visible) {
                DropdownMenu(
                    modifier = Modifier.width(252.dp),
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false },
                    shape = RoundedCornerShape(size = 16.dp),
                    containerColor = colorResource(id = R.color.background_primary),
                    shadowElevation = 5.dp
                ) {
                    AllContentMenu(
                        uiMenuState = uiMenuState,
                        onModeClick = onModeClick,
                        onSortClick = onSortClick,
                        onBinClick = onBinClick
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = colorResource(id = R.color.background_primary)
        ),
    )
}

@DefaultPreviews
@Composable
private fun AllContentTopBarContainerPreview() {
    AllContentTopBarContainer(
        titleState = UiTitleState.OnlyUnlinked,
        uiMenuState = UiMenuState.Visible(
            mode = listOf(
                AllContentMenuMode.AllContent(isSelected = true),
                AllContentMenuMode.Unlinked()
            ),
            container = MenuSortsItem.Container(
                sort = ObjectsListSort.ByName(isSelected = true)
            ),
            sorts = listOf(
                MenuSortsItem.Sort(
                    sort = ObjectsListSort.ByName(isSelected = true)
                ),
            ),
            types = listOf(
                MenuSortsItem.SortType(
                    sort = ObjectsListSort.ByName(isSelected = true),
                    sortType = DVSortType.DESC,
                    isSelected = true
                ),
                MenuSortsItem.SortType(
                    sort = ObjectsListSort.ByDateCreated(isSelected = false),
                    sortType = DVSortType.ASC,
                    isSelected = false
                ),
            )
        ),
        onModeClick = {},
        onSortClick = {},
        onBinClick = {},
        onBackClick = {}
    )
}
//endregion

//region AllContentTitle
@Composable
fun AllContentTitle(state: UiTitleState) {
    when (state) {
        UiTitleState.AllContent -> {
            Text(
                modifier = Modifier
                    .wrapContentSize(),
                text = stringResource(id = R.string.all_content_title_all_content),
                style = Title1,
                color = colorResource(id = R.color.text_primary)
            )
        }

        UiTitleState.OnlyUnlinked -> {
            Text(
                modifier = Modifier
                    .wrapContentSize(),
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
fun AllContentMenuButton(onClick: () -> Unit) {
    Image(
        modifier = Modifier
            .padding(end = 12.dp)
            .size(32.dp)
            .bouncingClickable { onClick() },
        painter = painterResource(id = R.drawable.ic_space_list_dots),
        contentDescription = "Menu icon",
        contentScale = ContentScale.Inside
    )
}
//endregion

//region AllContentTabs
@Composable
fun AllContentTabs(
    tabsViewState: UiTabsState,
    onClick: (AllContentTab) -> Unit
) {
    val scrollState = rememberLazyListState()
    var selectedTab by remember { mutableStateOf(tabsViewState.selectedTab) }

    val snapFlingBehavior = rememberSnapFlingBehavior(scrollState)

    LazyRow(
        state = scrollState,
        //todo Disabled because, after the scroll animation, the tabs sometimes don’t respond to clicks
        //flingBehavior = snapFlingBehavior,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp)
    ) {
        items(
            count = tabsViewState.tabs.size,
            key = { index -> tabsViewState.tabs[index].ordinal },
        ) { index ->
            val tab = tabsViewState.tabs[index]
            AllContentTabText(
                tab = tab,
                isSelected = tab == selectedTab,
                onClick = {
                    selectedTab = tab
                    onClick(tab)
                }
            )
        }
    }
}

@Composable
private fun AllContentTabText(
    tab: AllContentTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .wrapContentWidth()
            .height(40.dp)
            .noRippleThrottledClickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp),
            text = getTabText(tab),
            style = Title2,
            color = if (isSelected) colorResource(id = R.color.glyph_button) else colorResource(id = R.color.glyph_active),
            maxLines = 1
        )
    }
}

@Composable
private fun getTabText(tab: AllContentTab): String {
    return when (tab) {
        AllContentTab.PAGES -> stringResource(id = R.string.all_content_title_tab_pages)
        AllContentTab.FILES -> stringResource(id = R.string.all_content_title_tab_files)
        AllContentTab.MEDIA -> stringResource(id = R.string.all_content_title_tab_media)
        AllContentTab.BOOKMARKS -> stringResource(id = R.string.all_content_title_tab_bookmarks)
        AllContentTab.TYPES -> stringResource(id = R.string.all_content_title_tab_objetc_types)
        AllContentTab.LISTS -> stringResource(id = R.string.all_content_title_tab_lists)
        AllContentTab.RELATIONS -> stringResource(id = R.string.all_content_title_tab_relations)
    }
}

@DefaultPreviews
@Composable
private fun AllContentTabsPreview() {
    AllContentTabs(
        tabsViewState = UiTabsState(
            tabs = listOf(
                AllContentTab.PAGES,
                AllContentTab.FILES,
                AllContentTab.MEDIA,
                AllContentTab.BOOKMARKS,
                AllContentTab.TYPES,
                AllContentTab.RELATIONS
            ),
            selectedTab = AllContentTab.MEDIA
        ),
        onClick = {}
    )
}

//endregion

//region SearchBar
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AllContentSearchBar(onQueryChanged: (String) -> Unit) {

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
}

@DefaultPreviews
@Composable
private fun AllContentSearchBarPreview() {
    AllContentSearchBar() {}
}
//endregion