package com.anytypeio.anytype.ui.widgets.collection

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.keyboardAsState
import com.anytypeio.anytype.core_ui.foundation.components.BottomNavigationMenu
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.core_ui.widgets.CollectionActionWidget
import com.anytypeio.anytype.core_ui.widgets.ObjectIconWidget
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.setVisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.widgets.collection.CollectionObjectView
import com.anytypeio.anytype.presentation.widgets.collection.CollectionUiState
import com.anytypeio.anytype.presentation.widgets.collection.CollectionView
import com.anytypeio.anytype.presentation.widgets.collection.CollectionView.EmptySearch
import com.anytypeio.anytype.presentation.widgets.collection.CollectionView.ObjectView
import com.anytypeio.anytype.presentation.widgets.collection.CollectionView.SectionView
import com.anytypeio.anytype.presentation.widgets.collection.CollectionViewModel
import com.anytypeio.anytype.ui.search.ObjectSearchFragment
import com.anytypeio.anytype.ui.settings.typography
import com.google.accompanist.themeadapter.material.createMdcTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ScreenContent(
    vm: CollectionViewModel,
    uiState: CollectionUiState,
    onCreateObjectLongClicked: () -> Unit
) {
    Box(
        Modifier.background(color = colorResource(R.color.background_primary))
    )
    {
        Box(
            modifier = if (BuildConfig.USE_EDGE_TO_EDGE)
                Modifier.windowInsetsPadding(WindowInsets.systemBars)
            else
                Modifier
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(0.dp, 0.dp, 0.dp, 32.dp)
            ) {
                TopBar(vm, uiState)
                SearchBar(vm, uiState)
                ListView(vm, uiState, stringResource(id = R.string.search_no_results_try))
            }
            Box(Modifier.align(BottomCenter)) {
                BottomNavigationMenu(
                    backClick = { vm.onPrevClicked() },
                    homeClick = { vm.onHomeClicked() },
                    searchClick = { vm.onSearchClicked() },
                    addDocClick = { vm.onAddClicked(null) },
                    onCreateObjectLongClicked = onCreateObjectLongClicked,
                    onProfileClicked = vm::onProfileClicked,
                    profileIcon = vm.icon.collectAsState().value
                )
            }
        }
        if (uiState.operationInProgress) {
            LinearProgressIndicator(
                modifier = Modifier
                    .align(BottomCenter)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
fun TopBar(
    vm: CollectionViewModel,
    uiState: CollectionUiState
) {
    Box(
        modifier = Modifier
            .padding(16.dp, 0.dp)
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = uiState.collectionName,
            style = Title1,
            color = colorResource(id = R.color.text_primary)
        )
        Text(
            modifier = Modifier
                .align(CenterEnd)
                .noRippleClickable { vm.onActionClicked() }
                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp),
            text = uiState.actionName,
            style = UXBody,
            color = colorResource(id = R.color.glyph_active)
        )
    }
}

@Composable
fun ListView(
    vm: CollectionViewModel,
    uiState: CollectionUiState,
    emptySearchSubtitle: String? = null,
    itemBackground: Color = colorResource(id = R.color.background_primary)
) {

    val views = remember {
        mutableStateOf<List<CollectionView>>(listOf())
    }
    val lazyListState = rememberReorderableLazyListState(
        onMove = { from, to ->
            views.value = views.value.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        },
        onDragEnd = { from, to -> vm.onMove(views.value, from, to) },
    )

    uiState.views.fold(
        onSuccess = { list ->
            views.value = list
            LazyColumn(
                state = lazyListState.listState,
                modifier = Modifier
                    .reorderable(lazyListState)
                    .fillMaxHeight()
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 180.dp)
            ) {
                items(items = views.value, key = {
                    when (it) {
                        is ObjectView -> it.obj.id
                        is CollectionView.FavoritesView -> it.obj.id
                        is SectionView -> it.name
                        is EmptySearch -> it.query
                    }
                }) { item ->
                    when (item) {
                        is CollectionObjectView -> {
                            ReorderableItem(
                                lazyListState,
                                key = item.obj.id,
                            ) { isDragging ->
                                val alpha = animateFloatAsState(if (isDragging) 0.8f else 1.0f)
                                Column(
                                    modifier = Modifier
                                        .then(
                                            if (uiState.inDragMode) {
                                                Modifier.detectReorderAfterLongPress(
                                                    lazyListState
                                                )
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .background(itemBackground)
                                        .alpha(alpha.value)
                                ) {
                                    CollectionItem(
                                        view = item,
                                        inEditMode = uiState.showEditMode,
                                        inDragMode = uiState.inDragMode,
                                        displayType = uiState.displayType,
                                        onClick = { vm.onObjectClicked(item) },
                                        onLongClick = { vm.onObjectLongClicked(item) })
                                }
                            }
                        }
                        is SectionView -> {
                            ReorderableItem(
                                lazyListState,
                                key = item.name,
                            ) { isDragging ->
                                SectionItem(item)
                            }
                        }
                        is EmptySearch -> {
                            Box(
                                modifier = Modifier.fillParentMaxHeight()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Text(
                                        text = stringResource(
                                            R.string.search_no_results,
                                            item.query
                                        ),
                                        style = UXBody,
                                        color = colorResource(id = R.color.text_primary),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    emptySearchSubtitle?.let {
                                        Text(
                                            text = emptySearchSubtitle,
                                            style = UXBody,
                                            color = colorResource(id = R.color.text_secondary),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun SearchBar(
    vm: CollectionViewModel,
    uiState: CollectionUiState
) {
    val isKeyboardOpen by keyboardAsState()

    AndroidView(
        modifier = Modifier
            .padding(16.dp, 6.dp)
            .fillMaxWidth(),
        factory = { context ->
            val view = LayoutInflater.from(context)
                .inflate(R.layout.widget_search_view, null)
            val clearSearchText = view.findViewById<View>(R.id.clearSearchText)
            val filterInputField = view.findViewById<EditText>(R.id.filterInputField)
            filterInputField.setHint(R.string.search)
            filterInputField.imeOptions = EditorInfo.IME_ACTION_DONE
            filterInputField.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    return@setOnEditorActionListener false
                }
                true
            }
            clearSearchText.setOnClickListener {
                filterInputField.setText(ObjectSearchFragment.EMPTY_FILTER_TEXT)
                clearSearchText.invisible()
            }
            filterInputField.doAfterTextChanged { newText ->
                if (newText != null) {
                    vm.onSearchTextChanged(newText.toString())
                }
                if (newText.isNullOrEmpty()) {
                    clearSearchText.invisible()
                } else {
                    clearSearchText.visible()
                }
            }
            view
        },
        update = {
            it.findViewById<View>(R.id.progressBar).setVisible(uiState.views.isLoading)
        }
    )

    val currentView = LocalView.current

    LaunchedEffect(isKeyboardOpen) {
        snapshotFlow { isKeyboardOpen }
            .distinctUntilChanged()
            .collectLatest { isOpen ->
                if (!isOpen) {
                    currentView.findViewById<View>(R.id.filterInputField).clearFocus()
                }
            }
    }
}

@Composable
fun Icon(icon: ObjectIcon?) {
    icon?.let {
        AndroidView(factory = { ctx ->
            val iconWidget = LayoutInflater.from(ctx)
                .inflate(R.layout.collections_icon, null) as ObjectIconWidget
            iconWidget.setIcon(it)
            iconWidget
        })
    }
}

@Composable
fun SectionItem(
    view: SectionView
) {

    Box(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {

        Text(
            modifier = Modifier.padding(16.dp, 20.dp, 0.dp, 0.dp),
            text = view.name,
            style = Caption1Regular,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = colorResource(id = R.color.text_secondary)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun CollectionItem(
    view: CollectionObjectView,
    inEditMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    inDragMode: Boolean,
    displayType: Boolean,
) {

    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .then(
                if (inDragMode) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier.combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                }
            )
    ) {


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp, 0.dp, 0.dp)
        ) {

            AnimatedContent(
                modifier = Modifier
                    .align(CenterVertically),
                targetState = inEditMode,
                transitionSpec = {
                    fadeIn() + slideInHorizontally() with
                            fadeOut() + slideOutHorizontally()
                }
            ) { inEditMode ->
                if (inEditMode) {

                    val icon =
                        if (view.isSelected) R.drawable.checkbox_collections_checked else
                            R.drawable.checkbox_collections_unchecked

                    Box(
                        modifier = Modifier
                            .align(CenterVertically)
                            .padding(0.dp, 0.dp, 10.dp, 0.dp)
                            .width(34.dp),
                    ) {
                        Image(
                            painter = painterResource(icon),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(24.dp),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .padding(0.dp, 12.dp, 12.dp, 12.dp)
                    .size(48.dp)
                    .align(CenterVertically)
            ) {
                Icon(icon = view.obj.icon)
            }

            Column(
                modifier = Modifier
                    .align(CenterVertically)
                    .padding(0.dp, 0.dp, 60.dp, 0.dp)
            ) {

                val name = view.obj.name.trim().ifBlank { stringResource(R.string.untitled) }

                Text(
                    text = name,
                    style = PreviewTitle2Medium,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val description = view.obj.description
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        style = Relations3,
                        color = colorResource(id = R.color.text_primary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (displayType) {
                    val typeName = view.obj.typeName
                    if (!typeName.isNullOrBlank()) {
                        Text(
                            text = typeName,
                            style = Relations3,
                            color = colorResource(id = R.color.text_secondary),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = inDragMode,
            modifier = Modifier
                .align(CenterEnd),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .align(CenterEnd)
                    .width(60.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.burger),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp),
                )
            }
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .align(BottomCenter)
                .padding(16.dp, 0.dp, 16.dp, 0.dp),
            thickness = 0.5.dp,
            color = colorResource(R.color.shape_primary)
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun CollectionScreen(
    vm: CollectionViewModel,
    onCreateObjectLongClicked: () -> Unit
) {

    val uiState by vm.uiState.collectAsStateWithLifecycle()

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val localDensity = LocalDensity.current

    val coroutineScope = rememberCoroutineScope()

    uiState.fold(
        onSuccess = { state ->
            BottomSheetScaffold(
                sheetElevation = 0.dp,
                sheetBackgroundColor = Color.Transparent,
                scaffoldState = bottomSheetScaffoldState,
                sheetContent = { BlockWidget(localDensity, vm, state) },
                sheetPeekHeight = 0.dp
            ) {
                ScreenContent(vm, state, onCreateObjectLongClicked)

                LaunchedEffect(state) {

                        if (state.showWidget && bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.expand()
                            }
                        } else if (!state.showWidget && bottomSheetScaffoldState.bottomSheetState.isExpanded) {
                            coroutineScope.launch { bottomSheetScaffoldState.bottomSheetState.collapse() }
                        }

                    snapshotFlow { bottomSheetScaffoldState.bottomSheetState.currentValue }
                        .distinctUntilChanged()
                        .drop(1)
                        .collect {
                            vm.omBottomSheet(it == BottomSheetValue.Expanded)
                        }
                }

                BackHandler {
                    if (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.collapse()
                        }
                    }
                    vm.onBackPressed(bottomSheetScaffoldState.bottomSheetState.isExpanded)
                }
            }
        }
    )
}

@Composable
private fun BlockWidget(
    localDensity: Density,
    vm: CollectionViewModel,
    uiState: CollectionUiState
) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .absolutePadding(16.dp, 0.dp, 16.dp, 0.dp),
        factory = { context ->
            CollectionActionWidget(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setCardBackgroundColor(context.getColor(R.color.background_secondary))
                radius = with(localDensity) { 16.dp.toPx() }
                cardElevation = with(localDensity) { 16.dp.toPx() }
                useCompatPadding = true
                actionListener = { action -> vm.onActionWidgetClicked(action) }
            }
        }, update = {
            it.bind(uiState.objectActions)
        }
    )
}

@Composable
fun DefaultTheme(
    content: @Composable () -> Unit
) {
    val theme = createMdcTheme(
        context = LocalContext.current,
        layoutDirection = LocalLayoutDirection.current
    )

    MaterialTheme(
        colors = theme.colors ?: MaterialTheme.colors,
        typography = typography,
        shapes = theme.shapes ?: MaterialTheme.shapes,
        content = content
    )
}