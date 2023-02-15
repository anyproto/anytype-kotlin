package com.anytypeio.anytype.ui.library.views.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.library.DependentData
import com.anytypeio.anytype.presentation.library.LibraryEvent
import com.anytypeio.anytype.presentation.library.LibraryScreenState
import com.anytypeio.anytype.presentation.library.LibraryView
import com.anytypeio.anytype.ui.library.LibraryListConfig
import com.anytypeio.anytype.ui.library.ScreenState
import com.anytypeio.anytype.ui.library.WrapWithLibraryAnimation
import com.anytypeio.anytype.ui.library.views.list.LibraryListDefaults.SearchBarPadding
import com.anytypeio.anytype.ui.library.views.list.LibraryListDefaults.SearchBarPaddingTop
import com.anytypeio.anytype.ui.library.views.list.LibraryListDefaults.SearchCancelPaddingStart
import com.anytypeio.anytype.ui.library.views.list.LibraryListDefaults.SearchCancelPaddingTop
import com.anytypeio.anytype.ui.library.views.list.items.CreateNewTypeItem
import com.anytypeio.anytype.ui.library.views.list.items.ItemDefaults
import com.anytypeio.anytype.ui.library.views.list.items.LibRelationItem
import com.anytypeio.anytype.ui.library.views.list.items.LibTypeItem
import com.anytypeio.anytype.ui.library.views.list.items.LibraryTypesEmptyItem
import com.anytypeio.anytype.ui.library.views.list.items.MyRelationItem
import com.anytypeio.anytype.ui.library.views.list.items.MyTypeItem
import com.anytypeio.anytype.ui.library.views.list.items.noRippleClickable
import com.anytypeio.anytype.ui.settings.fonts
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState

@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalAnimationApi
@ExperimentalPagerApi
@Composable
fun LibraryListTabsContent(
    modifier: Modifier,
    pagerState: PagerState,
    configuration: List<LibraryListConfig>,
    tabs: LibraryScreenState.Tabs,
    vmEventStream: (LibraryEvent) -> Unit,
    screenState: MutableState<ScreenState>,
) {

    val itemModifier = Modifier
        .fillMaxWidth()
        .height(ItemDefaults.ITEM_HEIGHT)
        .padding(start = LibraryListDefaults.ItemPadding, end = LibraryListDefaults.ItemPadding)

    val keyboardController = LocalSoftwareKeyboardController.current

    HorizontalPager(
        modifier = modifier,
        state = pagerState,
        count = configuration.size,
        userScrollEnabled = screenState.value == ScreenState.CONTENT
    ) { index ->
        val config = configuration[index]
        val data = when (config) {
            is LibraryListConfig.Types, is LibraryListConfig.Relations -> tabs.my
            is LibraryListConfig.TypesLibrary, is LibraryListConfig.RelationsLibrary -> tabs.lib
        }
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            WrapWithLibraryAnimation(visible = screenState.value.visible().not()) {
                Spacer(modifier = Modifier.height(SearchBarPaddingTop))
            }
            Row(
                modifier = Modifier.padding(start = SearchBarPadding, end = SearchBarPadding)
            ) {
                LibraryListSearchWidget(
                    vmEventStream = vmEventStream,
                    config = configuration[index],
                    modifier = Modifier.weight(1f)
                )
                SearchCancel(
                    modifier = Modifier
                        .padding(start = SearchCancelPaddingStart, top = SearchCancelPaddingTop)
                        .noRippleClickable {
                            keyboardController?.hide()
                            screenState.value = ScreenState.CONTENT
                        },
                    visible = screenState.value.visible().not()
                )
            }
            LibraryList(data, itemModifier, vmEventStream, screenState)
        }
    }
}

@Composable
private fun SearchCancel(modifier: Modifier = Modifier, visible: Boolean = false) {
    AnimatedVisibility(visible = visible) {
        Text(
            modifier = modifier,
            text = stringResource(id = R.string.cancel),
            style = TextStyle(
                color = colorResource(id = R.color.text_secondary),
                fontSize = 17.sp,
                fontFamily = fonts
            ),
        )
    }

}

@Composable
private fun LibraryList(
    data: LibraryScreenState.Tabs.TabData,
    itemModifier: Modifier,
    vmEventStream: (LibraryEvent) -> Unit,
    screenState: MutableState<ScreenState>
) {

    val keyboardIsVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    LaunchedEffect(key1 = keyboardIsVisible) {
        if (keyboardIsVisible) {
            screenState.value = ScreenState.SEARCH
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = LibraryListDefaults.ListPadding,
            end = LibraryListDefaults.ListPadding
        )
    ) {

        items(
            count = data.items.size,
            key = { index -> data.items[index].id },
            itemContent = { ix ->
                when (val item = data.items[ix]) {
                    is LibraryView.LibraryTypeView -> {
                        LibTypeItem(
                            name = item.name,
                            icon = item.icon,
                            installed = item.dependentData is DependentData.Model,
                            modifier = itemModifier,
                            onClick = {
                                vmEventStream.invoke(
                                    LibraryEvent.ToggleInstall.Type(item)
                                )
                            }
                        )
                    }
                    is LibraryView.MyTypeView -> {
                        MyTypeItem(
                            name = item.name,
                            icon = item.icon,
                            readOnly = item.readOnly,
                            modifier = itemModifier.clickable {
                                vmEventStream.invoke(
                                    LibraryEvent.EditType(item)
                                )
                            }
                        )
                    }
                    is LibraryView.LibraryRelationView -> {
                        LibRelationItem(
                            modifier = itemModifier,
                            name = item.name,
                            format = item.format,
                            installed = item.dependentData is DependentData.Model,
                            onClick = {
                                vmEventStream.invoke(
                                    LibraryEvent.ToggleInstall.Relation(item)
                                )
                            }
                        )
                    }
                    is LibraryView.MyRelationView -> {
                        MyRelationItem(
                            modifier = itemModifier,
                            name = item.name,
                            readOnly = item.readOnly,
                            format = item.format
                        )
                    }
                    is LibraryView.CreateNewTypeView -> {
                        CreateNewTypeItem(
                            modifier = itemModifier.clickable {
                                vmEventStream.invoke(
                                    LibraryEvent.CreateType(
                                        item.name
                                    )
                                )
                            },
                            name = item.name
                        )
                    }
                    is LibraryView.LibraryTypesPlaceholderView -> {
                        LibraryTypesEmptyItem(item.name)
                    }
                    is LibraryView.UnknownView -> {
                        // do nothing
                    }
                }
                if (ix < data.items.lastIndex) {
                    LibraryDivider()
                }
            }
        )
    }
}

@Composable
private fun LibraryDivider() {
    Divider(
        thickness = LibraryListDefaults.DividerThickness,
        modifier = Modifier.padding(
            start = LibraryListDefaults.DividerPadding,
            end = LibraryListDefaults.DividerPadding
        ),
        color = colorResource(id = R.color.shape_primary)
    )
}

@Immutable
private object LibraryListDefaults {
    val ItemPadding = 4.dp
    val DividerPadding = 4.dp
    val ListPadding = 16.dp
    val DividerThickness = 0.5.dp
    val SearchBarPadding = 20.dp
    val SearchCancelPaddingStart = 8.dp
    val SearchCancelPaddingTop = 4.dp
    val SearchBarPaddingTop = 16.dp
}