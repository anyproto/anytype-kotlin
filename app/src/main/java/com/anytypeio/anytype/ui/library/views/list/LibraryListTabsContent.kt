package com.anytypeio.anytype.ui.library.views.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.presentation.library.DependentData
import com.anytypeio.anytype.presentation.library.LibraryAnalyticsEvent
import com.anytypeio.anytype.presentation.library.LibraryEvent
import com.anytypeio.anytype.presentation.library.LibraryScreenState
import com.anytypeio.anytype.presentation.library.LibraryView
import com.anytypeio.anytype.presentation.library.LibraryViewModel
import com.anytypeio.anytype.ui.library.LibraryListConfig
import com.anytypeio.anytype.ui.library.ScreenState
import com.anytypeio.anytype.ui.library.views.list.LibraryListDefaults.SearchBarPadding
import com.anytypeio.anytype.ui.library.views.list.LibraryListDefaults.SearchCancelPaddingStart
import com.anytypeio.anytype.ui.library.views.list.LibraryListDefaults.SearchCancelPaddingTop
import com.anytypeio.anytype.ui.library.views.list.items.CreateNewRelationItem
import com.anytypeio.anytype.ui.library.views.list.items.CreateNewTypeItem
import com.anytypeio.anytype.ui.library.views.list.items.ItemDefaults
import com.anytypeio.anytype.ui.library.views.list.items.LibRelationItem
import com.anytypeio.anytype.ui.library.views.list.items.LibTypeItem
import com.anytypeio.anytype.ui.library.views.list.items.LibraryObjectEmptyItem
import com.anytypeio.anytype.ui.library.views.list.items.MyRelationItem
import com.anytypeio.anytype.ui.library.views.list.items.MyTypeItem
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
    vmAnalyticsStream: (LibraryAnalyticsEvent.Ui) -> Unit,
    screenState: MutableState<ScreenState>,
    effects: LibraryViewModel.Effect,
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val animationStartState = remember {
        mutableStateOf(false)
    }
    val input = remember { mutableStateOf(String()) }

    HorizontalPager(
        modifier = modifier,
        state = pagerState,
        count = configuration.size,
        userScrollEnabled = screenState.value == ScreenState.CONTENT
    ) { index ->
        val data = when (configuration[index]) {
            is LibraryListConfig.Types, is LibraryListConfig.Relations -> {
                tabs.my
            }
            is LibraryListConfig.TypesLibrary, is LibraryListConfig.RelationsLibrary -> {
                tabs.lib
            }
        }
        val itemsListState = rememberLazyListState()
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier.padding(start = SearchBarPadding, end = SearchBarPadding)
            ) {
                LaunchedEffect(key1 = effects) {
                    if (effects is LibraryViewModel.Effect.ObjectCreated) {
                        input.value = ""
                        vmEventStream.invoke(
                            configuration[index].toEvent(input.value)
                        )
                        animationStartState.value = false
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        screenState.value = ScreenState.CONTENT
                        itemsListState.animateScrollToItem(0)
                    }
                }
                LibraryListSearchWidget(
                    vmEventStream = vmEventStream,
                    config = configuration[index],
                    modifier = Modifier.weight(1f),
                    screenState = screenState,
                    animationStartState = animationStartState,
                    input = input
                )
                SearchCancel(
                    modifier = Modifier
                        .padding(start = SearchCancelPaddingStart, top = SearchCancelPaddingTop)
                        .noRippleClickable {
                            input.value = ""
                            vmEventStream.invoke(
                                configuration[index].toEvent(input.value)
                            )
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            animationStartState.value = false
                            screenState.value = ScreenState.CONTENT
                        },
                    visible = screenState.value.visible().not()
                )
            }
            LibraryList(data, vmEventStream, screenState, itemsListState)
        }
    }
    attachAnalytics(pagerState, configuration, vmAnalyticsStream)
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun attachAnalytics(
    pagerState: PagerState,
    configuration: List<LibraryListConfig>,
    vmAnalyticsStream: (LibraryAnalyticsEvent.Ui) -> Unit
) {
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            when (configuration[page]) {
                is LibraryListConfig.Types -> {
                    vmAnalyticsStream.invoke(LibraryAnalyticsEvent.Ui.TabView.Types)
                }
                is LibraryListConfig.Relations -> {
                    vmAnalyticsStream.invoke(LibraryAnalyticsEvent.Ui.TabView.Relations)
                }
                is LibraryListConfig.TypesLibrary -> {
                    vmAnalyticsStream.invoke(LibraryAnalyticsEvent.Ui.TabView.LibTypes)
                }
                is LibraryListConfig.RelationsLibrary -> {
                    vmAnalyticsStream.invoke(LibraryAnalyticsEvent.Ui.TabView.LibRelations)
                }
            }
        }
    }
}

@Composable
private fun SearchCancel(modifier: Modifier = Modifier, visible: Boolean = false) {
    AnimatedVisibility(visible = visible) {
        Text(
            modifier = modifier,
            text = stringResource(id = R.string.cancel),
            style = UXBody.copy(
                color = colorResource(id = R.color.glyph_active)
            )
        )
    }

}

@Composable
private fun LibraryList(
    data: LibraryScreenState.Tabs.TabData,
    vmEventStream: (LibraryEvent) -> Unit,
    screenState: MutableState<ScreenState>,
    itemsListState: LazyListState,
) {

    val itemModifier = Modifier
        .fillMaxWidth()
        .height(ItemDefaults.ITEM_HEIGHT)

    LazyColumn(modifier = Modifier.fillMaxSize(), state = itemsListState) {
        items(
            count = data.items.size,
            key = { index -> "library-item-${data.items[index].id}" },
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
                            modifier = itemModifier.clickable(item.editable) {
                                vmEventStream.invoke(
                                    LibraryEvent.Type.Edit(item)
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
                            modifier = itemModifier.clickable(item.editable) {
                                vmEventStream.invoke(
                                    LibraryEvent.Relation.Edit(item)
                                )
                            },
                            name = item.name,
                            readOnly = item.readOnly,
                            format = item.format
                        )
                    }
                    is LibraryView.CreateNewTypeView -> {
                        CreateNewTypeItem(
                            modifier = itemModifier.clickable {
                                vmEventStream.invoke(
                                    LibraryEvent.Type.Create(item.name)
                                )
                            },
                            name = item.name
                        )
                    }
                    is LibraryView.CreateNewRelationView -> {
                        CreateNewRelationItem(
                            modifier = itemModifier.clickable {
                                vmEventStream.invoke(
                                    LibraryEvent.Relation.Create(item.name)
                                )
                            },
                            name = item.name
                        )
                    }
                    is LibraryView.LibraryTypesPlaceholderView -> {
                        LibraryObjectEmptyItem(LibraryObjectTypes.TYPES.type, item.name)
                    }
                    is LibraryView.LibraryRelationsPlaceholderView -> {
                        LibraryObjectEmptyItem(LibraryObjectTypes.RELATIONS.type, item.name)
                    }
                    is LibraryView.UnknownView -> {
                        // do nothing
                    }
                }
                if (ix < data.items.lastIndex) {
                    LibraryDivider()
                }
                if (ix == data.items.lastIndex && screenState.value.visible()) {
                    Spacer(modifier = Modifier.height(48.dp))
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
internal object LibraryListDefaults {
    val ItemPadding = 20.dp
    val DividerPadding = 20.dp
    val DividerThickness = 0.5.dp
    val SearchBarPadding = 20.dp
    val SearchCancelPaddingStart = 8.dp
    val SearchCancelPaddingTop = 12.dp
}

enum class LibraryObjectTypes(val type: String) {
    TYPES("types"), RELATIONS("relations")
}