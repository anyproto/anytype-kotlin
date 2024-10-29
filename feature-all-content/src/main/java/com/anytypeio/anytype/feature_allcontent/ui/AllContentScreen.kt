package com.anytypeio.anytype.feature_allcontent.ui

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.simpleIcon
import com.anytypeio.anytype.core_ui.extensions.swapList
import com.anytypeio.anytype.core_ui.foundation.DismissBackground
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.components.BottomNavigationMenu
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.core_ui.views.animations.DotsLoadingIndicator
import com.anytypeio.anytype.core_ui.views.animations.FadeAnimationSpecs
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.feature_allcontent.BuildConfig
import com.anytypeio.anytype.feature_allcontent.R
import com.anytypeio.anytype.feature_allcontent.models.AllContentBottomMenu
import com.anytypeio.anytype.feature_allcontent.models.AllContentMenuMode
import com.anytypeio.anytype.feature_allcontent.models.AllContentSort
import com.anytypeio.anytype.feature_allcontent.models.AllContentTab
import com.anytypeio.anytype.feature_allcontent.models.UiContentItem
import com.anytypeio.anytype.feature_allcontent.models.UiContentState
import com.anytypeio.anytype.feature_allcontent.models.UiItemsState
import com.anytypeio.anytype.feature_allcontent.models.UiMenuState
import com.anytypeio.anytype.feature_allcontent.models.UiSnackbarState
import com.anytypeio.anytype.feature_allcontent.models.UiTabsState
import com.anytypeio.anytype.feature_allcontent.models.UiTitleState
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun AllContentWrapperScreen(
    uiTitleState: UiTitleState,
    uiTabsState: UiTabsState,
    uiMenuState: UiMenuState,
    uiSnackbarState: UiSnackbarState,
    uiItemsState: UiItemsState,
    uiBottomMenu: AllContentBottomMenu,
    onTabClick: (AllContentTab) -> Unit,
    onQueryChanged: (String) -> Unit,
    onModeClick: (AllContentMenuMode) -> Unit,
    onSortClick: (AllContentSort) -> Unit,
    onItemClicked: (UiContentItem.Item) -> Unit,
    onTypeClicked: (UiContentItem) -> Unit,
    onRelationClicked: (UiContentItem) -> Unit,
    onBinClick: () -> Unit,
    canPaginate: Boolean,
    onUpdateLimitSearch: () -> Unit,
    uiContentState: UiContentState,
    onGlobalSearchClicked: () -> Unit,
    onAddDocClicked: () -> Unit,
    onCreateObjectLongClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onBackLongClicked: () -> Unit,
    moveToBin: (UiContentItem.Item) -> Unit,
    undoMoveToBin: (Id) -> Unit,
    onDismissSnackbar: () -> Unit
) {

    AllContentMainScreen(
        uiTitleState = uiTitleState,
        uiTabsState = uiTabsState,
        uiSnackbarState = uiSnackbarState,
        onTabClick = onTabClick,
        onQueryChanged = onQueryChanged,
        uiMenuState = uiMenuState,
        onModeClick = onModeClick,
        onSortClick = onSortClick,
        onItemClicked = onItemClicked,
        onBinClick = onBinClick,
        uiItemsState = uiItemsState,
        uiContentState = uiContentState,
        onTypeClicked = onTypeClicked,
        onGlobalSearchClicked = onGlobalSearchClicked,
        onAddDocClicked = onAddDocClicked,
        onCreateObjectLongClicked = onCreateObjectLongClicked,
        onBackClicked = onBackClicked,
        onBackLongClicked = onBackLongClicked,
        moveToBin = moveToBin,
        onRelationClicked = onRelationClicked,
        uiBottomMenu = uiBottomMenu,
        undoMoveToBin = undoMoveToBin,
        onDismissSnackbar = onDismissSnackbar,
        canPaginate = canPaginate,
        onUpdateLimitSearch = onUpdateLimitSearch
    )
}


@Composable
fun AllContentMainScreen(
    uiItemsState: UiItemsState,
    uiTitleState: UiTitleState,
    uiTabsState: UiTabsState,
    uiMenuState: UiMenuState,
    uiSnackbarState: UiSnackbarState,
    uiBottomMenu: AllContentBottomMenu,
    onTabClick: (AllContentTab) -> Unit,
    onQueryChanged: (String) -> Unit,
    onModeClick: (AllContentMenuMode) -> Unit,
    onSortClick: (AllContentSort) -> Unit,
    onItemClicked: (UiContentItem.Item) -> Unit,
    onTypeClicked: (UiContentItem) -> Unit,
    onRelationClicked: (UiContentItem) -> Unit,
    onBinClick: () -> Unit,
    uiContentState: UiContentState,
    onGlobalSearchClicked: () -> Unit,
    onAddDocClicked: () -> Unit,
    onCreateObjectLongClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onBackLongClicked: () -> Unit,
    moveToBin: (UiContentItem.Item) -> Unit,
    undoMoveToBin: (Id) -> Unit,
    onDismissSnackbar: () -> Unit,
    canPaginate: Boolean,
    onUpdateLimitSearch: () -> Unit,
) {
    var isSearchEmpty by remember { mutableStateOf(true) }
    val snackBarHostState = remember { SnackbarHostState() }

    val snackBarText = stringResource(R.string.all_content_snackbar_title)
    val undoText = stringResource(R.string.undo)

    LaunchedEffect(key1 = uiSnackbarState) {
        if (uiSnackbarState is UiSnackbarState.Visible) {
            ShowMoveToBinSnackbar(
                message = "'${uiSnackbarState.message}' $snackBarText",
                undo = undoText,
                scope = this,
                snackBarHostState = snackBarHostState,
                objectId = uiSnackbarState.objId,
                undoMoveToBin = undoMoveToBin,
                onDismissSnackbar = onDismissSnackbar
            )
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = colorResource(id = R.color.background_primary),
        bottomBar = {
            Box(
                modifier = if (BuildConfig.USE_EDGE_TO_EDGE && Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                    Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(bottom = 20.dp)
                        .fillMaxWidth()
                else
                    Modifier
                        .padding(bottom = 20.dp)
                        .fillMaxWidth()
            ) {
                BottomMenu(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onGlobalSearchClicked = onGlobalSearchClicked,
                    onAddDocClicked = onAddDocClicked,
                    onCreateObjectLongClicked = onCreateObjectLongClicked,
                    onBackClicked = onBackClicked,
                    onBackLongClicked = onBackLongClicked,
                    uiBottomMenu = uiBottomMenu
                )
            }
        },
        topBar = {
            Column(
                modifier = if (BuildConfig.USE_EDGE_TO_EDGE && Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                    Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .fillMaxWidth()
                else
                    Modifier.fillMaxWidth()
            ) {
                AllContentTopBarContainer(
                    titleState = uiTitleState,
                    uiMenuState = uiMenuState,
                    onSortClick = onSortClick,
                    onModeClick = onModeClick,
                    onBinClick = onBinClick
                )
                AllContentTabs(tabsViewState = uiTabsState) { tab ->
                    onTabClick(tab)
                }
                Spacer(modifier = Modifier.size(10.dp))
                AllContentSearchBar(onQueryChanged = {
                    isSearchEmpty = it.isEmpty()
                    onQueryChanged(it)
                })
                Spacer(modifier = Modifier.size(10.dp))
                Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
            }
        },
        content = { paddingValues ->
            val contentModifier =
                if (BuildConfig.USE_EDGE_TO_EDGE && Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                    Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
                        .background(color = colorResource(id = R.color.background_primary))
                else
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(color = colorResource(id = R.color.background_primary))

            Box(
                modifier = contentModifier,
                contentAlignment = Alignment.Center
            ) {
                when (uiItemsState) {
                    UiItemsState.Empty -> {
                        when (uiContentState) {
                            is UiContentState.Error -> {
                                ErrorState(uiContentState.message)
                            }

                            is UiContentState.Idle -> {
                                // Do nothing.
                            }

                            UiContentState.InitLoading -> {
                                LoadingState()
                            }

                            UiContentState.Paging -> {}
                            UiContentState.Empty -> {
                                EmptyState(isSearchEmpty = isSearchEmpty)
                            }
                        }
                    }

                    is UiItemsState.Content -> {
                        ContentItems(
                            uiItemsState = uiItemsState,
                            onItemClicked = onItemClicked,
                            onTypeClicked = onTypeClicked,
                            uiContentState = uiContentState,
                            moveToBin = moveToBin,
                            onRelationClicked = onRelationClicked,
                            canPaginate = canPaginate,
                            onUpdateLimitSearch = onUpdateLimitSearch
                        )
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    )
}

@Composable
fun BottomMenu(
    uiBottomMenu: AllContentBottomMenu,
    modifier: Modifier = Modifier,
    onGlobalSearchClicked: () -> Unit,
    onAddDocClicked: () -> Unit,
    onCreateObjectLongClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onBackLongClicked: () -> Unit,
) {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    if (isImeVisible) return
    BottomNavigationMenu(
        modifier = modifier,
        backClick = onBackClicked,
        backLongClick = onBackLongClicked,
        searchClick = onGlobalSearchClicked,
        addDocClick = onAddDocClicked,
        addDocLongClick = onCreateObjectLongClicked,
        isOwnerOrEditor = uiBottomMenu.isOwnerOrEditor
    )
}

@Composable
private fun ContentItems(
    uiItemsState: UiItemsState.Content,
    onItemClicked: (UiContentItem.Item) -> Unit,
    onTypeClicked: (UiContentItem) -> Unit,
    onRelationClicked: (UiContentItem) -> Unit,
    uiContentState: UiContentState,
    canPaginate: Boolean,
    moveToBin: (UiContentItem.Item) -> Unit,
    onUpdateLimitSearch: () -> Unit
) {
    val items = remember { mutableStateListOf<UiContentItem>() }
    items.swapList(uiItemsState.items)

    val scope = rememberCoroutineScope()

    val lazyListState = rememberLazyListState()

    val canPaginateState = remember { mutableStateOf(false) }
    LaunchedEffect(key1 = canPaginate) {
        canPaginateState.value = canPaginate
    }

    val shouldStartPaging = remember {
        derivedStateOf {
            canPaginateState.value && (lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: -9) >= (lazyListState.layoutInfo.totalItemsCount - 2)
        }
    }

    LaunchedEffect(key1 = shouldStartPaging.value) {
        if (shouldStartPaging.value && uiContentState is UiContentState.Idle) {
            onUpdateLimitSearch()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState
    ) {
        items(
            count = items.size,
            key = { index -> items[index].id },
            contentType = { index ->
                when (items[index]) {
                    is UiContentItem.Group -> "group"
                    is UiContentItem.Item -> "item"
                    is UiContentItem.Type -> "type"
                    is UiContentItem.Relation -> "relation"
                    UiContentItem.NewRelation -> "new_relation"
                    UiContentItem.NewType -> "new_type"
                    UiContentItem.UnlinkedDescription -> "unlinked_description"
                }
            }
        ) { index ->
            when (val item = items[index]) {
                is UiContentItem.Group -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .animateItem(),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text(
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(start = 20.dp, bottom = 8.dp),
                            text = item.title(),
                            style = Caption1Regular,
                            color = colorResource(id = R.color.text_secondary),
                        )
                    }
                }

                is UiContentItem.Item -> {
                    SwipeToDismissListItems(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .animateItem()
                            .noRippleClickable {
                                onItemClicked(item)
                            },
                        item = item,
                        moveToBin = { moveToBin(it) }
                    )
                    Divider(paddingStart = 16.dp, paddingEnd = 16.dp)
                }

                is UiContentItem.Type -> {
                    Type(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .bottomBorder()
                            .animateItem()
                            .noRippleThrottledClickable {
                                onTypeClicked(item)
                            },
                        item = item
                    )
                }

                is UiContentItem.Relation -> {
                    Relation(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .bottomBorder()
                            .animateItem()
                            .noRippleThrottledClickable {
                                onRelationClicked(item)
                            },
                        item = item
                    )
                }

                UiContentItem.NewRelation -> {
                    AddItem(
                        modifier = Modifier
                            .clickable { onRelationClicked(item) },
                        text = stringResource(id = R.string.all_content_new_relation)
                    )
                    Divider(paddingStart = 16.dp, paddingEnd = 16.dp)
                }

                UiContentItem.NewType -> {
                    AddItem(
                        modifier = Modifier
                            .clickable { onTypeClicked(item) },
                        text = stringResource(id = R.string.all_content_new_type)
                    )
                    Divider(paddingStart = 16.dp, paddingEnd = 16.dp)
                }

                UiContentItem.UnlinkedDescription -> {
                    UnlinkedDescription()
                    Divider(paddingStart = 16.dp, paddingEnd = 16.dp)
                }
            }
        }
        if (uiContentState is UiContentState.Paging) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .height(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingState()
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(200.dp))
        }
    }

    LaunchedEffect(key1 = uiContentState) {
        if (uiContentState is UiContentState.Idle) {
            if (uiContentState.scrollToTop) {
                scope.launch {
                    lazyListState.scrollToItem(0)
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.UnlinkedDescription() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(64.dp)
            .animateItem(),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = stringResource(id = R.string.all_content_unlinked_description),
            style = Caption1Regular,
            color = colorResource(id = R.color.text_secondary),
        )
    }
}

@Composable
private fun LazyItemScope.AddItem(modifier: Modifier, text: String) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 16.dp)
            .animateItem(),
        contentAlignment = Alignment.CenterStart
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_default_plus),
            contentDescription = text,
            modifier = Modifier.size(24.dp)
        )
        Text(
            modifier = Modifier.padding(start = 34.dp),
            text = text,
            style = BodyRegular,
            color = colorResource(id = R.color.text_secondary),
        )
    }
}

@Composable
private fun BoxScope.LoadingState() {
    val loadingAlpha by animateFloatAsState(targetValue = 1f, label = "")
    DotsLoadingIndicator(
        animating = true,
        modifier = Modifier
            .graphicsLayer { alpha = loadingAlpha }
            .align(Alignment.Center),
        animationSpecs = FadeAnimationSpecs(itemCount = 3),
        color = colorResource(id = R.color.glyph_active),
        size = ButtonSize.Small
    )
}

@DefaultPreviews
@Composable
fun PreviewLoadingState() {
    Box(modifier = Modifier.fillMaxSize()) {
        LoadingState()
    }
}

@DefaultPreviews
@Composable
fun PreviewMainScreen() {
    AllContentMainScreen(
        uiItemsState = UiItemsState.Empty,
        uiTitleState = UiTitleState.AllContent,
        uiTabsState = UiTabsState(
            tabs = listOf(
                AllContentTab.PAGES,
                AllContentTab.TYPES,
                AllContentTab.LISTS
            ), selectedTab = AllContentTab.LISTS
        ),
        uiMenuState = UiMenuState.Hidden,
        onTabClick = {},
        onQueryChanged = {},
        onModeClick = {},
        onSortClick = {},
        onItemClicked = {},
        onBinClick = {},
        uiContentState = UiContentState.Error("Error message"),
        onTypeClicked = {},
        onGlobalSearchClicked = {},
        onAddDocClicked = {},
        onCreateObjectLongClicked = {},
        onBackClicked = {},
        moveToBin = {},
        onBackLongClicked = {},
        onRelationClicked = {},
        uiBottomMenu = AllContentBottomMenu(isOwnerOrEditor = false),
        uiSnackbarState = UiSnackbarState.Hidden,
        undoMoveToBin = {},
        onDismissSnackbar = {},
        canPaginate = true,
        onUpdateLimitSearch = {}
    )
}

@Composable
fun RowScope.Item(
    item: UiContentItem.Item
) {
    val name = item.name.trim().ifBlank { stringResource(R.string.untitled) }
    val description = item.description
    val typeName = item.typeName
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = colorResource(id = R.color.background_primary),
        ),
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth(),
        headlineContent = {
            Column {
                Text(
                    text = name,
                    style = PreviewTitle2Medium,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        style = Relations3,
                        color = colorResource(id = R.color.text_primary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        supportingContent = {
            if (typeName != null) {
                Text(
                    text = typeName,
                    style = Relations3,
                    color = colorResource(id = R.color.text_secondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        leadingContent = {
            ListWidgetObjectIcon(icon = item.icon, modifier = Modifier, iconSize = 48.dp)
        }
    )
}

@Composable
private fun Type(
    modifier: Modifier,
    item: UiContentItem.Type
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(start = 0.dp, top = 14.dp, end = 14.dp, bottom = 14.dp)
                .wrapContentSize()
        ) {
            //todo delete !!
            ListWidgetObjectIcon(icon = item.icon!!, modifier = Modifier, iconSize = 24.dp)
        }
        val name = item.name.trim().ifBlank { stringResource(R.string.untitled) }

        Text(
            text = name,
            style = PreviewTitle1Medium,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun Relation(
    modifier: Modifier,
    item: UiContentItem.Relation
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(start = 0.dp, top = 14.dp, end = 12.dp, bottom = 14.dp)
                .wrapContentSize()
        ) {
            item.format.simpleIcon()?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = "Relation format icon",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        val name = item.name.trim().ifBlank { stringResource(R.string.untitled) }

        Text(
            text = name,
            style = PreviewTitle1Medium,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ErrorState(message: String) {
    Column(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            text = stringResource(id = R.string.all_content_error_title),
            color = colorResource(id = R.color.text_primary),
            style = UXBody,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            text = message,
            color = colorResource(id = R.color.palette_system_red),
            style = UXBody,
            textAlign = TextAlign.Center,
            maxLines = 3
        )
    }
}

@Composable
private fun EmptyState(isSearchEmpty: Boolean) {
    val (title, description) = if (!isSearchEmpty) {
        stringResource(R.string.all_content_no_results_title) to stringResource(R.string.all_content_no_results_description)
    } else {
        stringResource(R.string.allContent_empty_state_title) to stringResource(R.string.allContent_empty_state_description)
    }
    Column(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth(),
            text = title,
            color = colorResource(id = R.color.text_primary),
            style = UXBody,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier
                .fillMaxWidth(),
            text = description,
            color = colorResource(id = R.color.text_secondary),
            style = UXBody,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun UiContentItem.Group.title(): String {
    return when (this) {
        is UiContentItem.Group.Today -> stringResource(R.string.allContent_group_today)
        is UiContentItem.Group.Yesterday -> stringResource(R.string.allContent_group_yesterday)
        is UiContentItem.Group.Previous7Days -> stringResource(R.string.allContent_group_prev_7)
        is UiContentItem.Group.Previous14Days -> stringResource(R.string.allContent_group_prev_14)
        is UiContentItem.Group.Month -> title
        is UiContentItem.Group.MonthAndYear -> title
    }
}

object AllContentNavigation {
    const val ALL_CONTENT_MAIN = "all_content_main"
}

@Composable
fun Modifier.bottomBorder(
    strokeWidth: Dp = 0.5.dp,
    color: Color = colorResource(R.color.shape_primary)
) = composed(
    factory = {
        val density = LocalDensity.current
        val strokeWidthPx = density.run { strokeWidth.toPx() }

        Modifier.drawBehind {
            val width = size.width
            val height = size.height - strokeWidthPx / 2

            drawLine(
                color = color,
                start = Offset(x = 0f, y = height),
                end = Offset(x = width, y = height),
                strokeWidth = strokeWidthPx
            )
        }
    }
)

@Composable
fun SwipeToDismissListItems(
    item: UiContentItem.Item,
    modifier: Modifier,
    animationDuration: Int = 500,
    moveToBin: (UiContentItem.Item) -> Unit
) {
    var isRemoved by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        initialValue = SwipeToDismissBoxValue.Settled,
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                isRemoved = true
                true
            } else {
                false
            }
        },
        positionalThreshold = { it * .5f }
    )

    if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
        LaunchedEffect(Unit) {
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    LaunchedEffect(key1 = isRemoved) {
        if (isRemoved) {
            delay(animationDuration.toLong())
            moveToBin(item)
        }
    }
    AnimatedVisibility(
        visible = !isRemoved,
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = animationDuration),
            shrinkTowards = Alignment.Top
        ) + fadeOut()
    ) {
        SwipeToDismissBox(
            modifier = modifier,
            state = dismissState,
            enableDismissFromEndToStart = item.isPossibleToDelete,
            enableDismissFromStartToEnd = false,
            backgroundContent = {
                DismissBackground(
                    actionText = stringResource(R.string.move_to_bin),
                    dismissState = dismissState
                )
            },
            content = { Item(item = item) }
        )
    }
}

private fun ShowMoveToBinSnackbar(
    objectId: Id,
    message: String,
    undo: String,
    scope: CoroutineScope,
    snackBarHostState: SnackbarHostState,
    undoMoveToBin: (Id) -> Unit,
    onDismissSnackbar: () -> Unit
) {
    scope.launch {
        val result = snackBarHostState
            .showSnackbar(
                message = message,
                actionLabel = undo,
                duration = SnackbarDuration.Short,
                withDismissAction = true
            )
        when (result) {
            SnackbarResult.ActionPerformed -> {
                undoMoveToBin(objectId)
            }

            SnackbarResult.Dismissed -> {
                onDismissSnackbar()
            }
        }
    }
}

@DefaultPreviews
@Composable
fun MtSwipeToDismissListItems() {
    SwipeToDismissListItems(
        item = UiContentItem.Item(
            id = "1",
            name = "Name",
            description = "Description11",
            typeName = "Type11",
            icon = ObjectIcon.Basic.Emoji("📄"),
            space = SpaceId("1"),
        ),
        modifier = Modifier.fillMaxWidth(),
        moveToBin = {}
    )
}