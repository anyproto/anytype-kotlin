package com.anytypeio.anytype.feature_search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.DefaultSearchBar
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_ui.widgets.objectIcon.SpaceIconView
import com.anytypeio.anytype.feature_search.presentation.BrowseSort
import com.anytypeio.anytype.feature_search.presentation.ChipView
import com.anytypeio.anytype.feature_search.presentation.PickerRowView
import com.anytypeio.anytype.feature_search.presentation.SearchResultView
import com.anytypeio.anytype.feature_search.presentation.SearchViewModel
import com.anytypeio.anytype.feature_search.presentation.TokenView
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import com.anytypeio.anytype.localization.R as Loc

/**
 * Chip/pill label style: Android's default font padding sits the 13/18sp
 * caption below the optical center of a fixed-height capsule — trim it and
 * center the line box so iconless chips don't look off-center.
 */
private val ChipLabel = Caption1Medium.copy(
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onBackClicked: () -> Unit = {},
    focusOnStart: Boolean = true
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val tokens by viewModel.tokenViews.collectAsStateWithLifecycle()
    val chips by viewModel.chips.collectAsStateWithLifecycle()
    val query by viewModel.input.collectAsStateWithLifecycle()
    val selectedTokenId by viewModel.selectedTokenId.collectAsStateWithLifecycle()
    val activePicker by viewModel.activePicker.collectAsStateWithLifecycle()
    val pickerRows by viewModel.pickerRows.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background_primary))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        TokenSearchField(
            query = query,
            tokens = tokens,
            selectedTokenId = selectedTokenId,
            onQueryChanged = viewModel::onQueryChanged,
            onTokenClicked = { viewModel.onTokenClicked(it.token) },
            onTokenRemoved = { viewModel.onTokenRemoved(it.token) },
            onBackspaceWhenEmpty = viewModel::onBackspaceToken,
            onBackClicked = onBackClicked,
            focusOnStart = focusOnStart,
            modifier = Modifier.fillMaxWidth()
        )
        SearchChipsRow(
            chips = chips,
            onChipClicked = viewModel::onChipClicked,
            modifier = Modifier.fillMaxWidth()
        )
        SearchResultsList(
            state = state,
            viewModel = viewModel,
            modifier = Modifier.weight(1f)
        )
    }

    if (activePicker != null) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onPickerDismissed,
            containerColor = colorResource(R.color.background_secondary)
        ) {
            PickerSheetContent(
                rows = pickerRows,
                onRowClicked = viewModel::onPickerRowClicked
            )
        }
    }
}

//region Token field

/**
 * Gmail-style active search header: flat (same color as the header, no border,
 * no rounded container), a prominent back button on the left and a clear
 * button on the right. Pills: tap selects (accent fill), backspace on an
 * empty field selects-then-removes; while typing, pills with icons collapse
 * to their icon to free the bar for the text.
 */
@Composable
fun TokenSearchField(
    query: String,
    tokens: List<TokenView>,
    selectedTokenId: String?,
    onQueryChanged: (String) -> Unit,
    onTokenClicked: (TokenView) -> Unit,
    onTokenRemoved: (TokenView) -> Unit,
    onBackspaceWhenEmpty: () -> Unit,
    onBackClicked: () -> Unit,
    focusOnStart: Boolean,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    Row(
        modifier = modifier.height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back),
            contentDescription = "Back",
            modifier = Modifier
                .size(48.dp)
                .clickable { onBackClicked() }
                .padding(12.dp),
            tint = colorResource(R.color.glyph_active)
        )
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            // Measured overflow collapse (iOS §3.5): when the full labels plus
            // a usable text field cannot fit, EVERY pill with an icon drops to
            // its icon — all-or-nothing, truncated pill text says nothing.
            val labels = tokens.map { tokenLabel(it) }
            val density = LocalDensity.current
            val textMeasurer = rememberTextMeasurer()
            // textMeasurer is a key too: a density/font-scale change hands out
            // a new measurer, and the cached widths must not survive it.
            val overflows = remember(labels, tokens, constraints.maxWidth, textMeasurer) {
                if (tokens.isEmpty()) {
                    false
                } else {
                    var pillsWidth = 0f
                    tokens.forEachIndexed { index, token ->
                        val textWidth = textMeasurer
                            .measure(AnnotatedString(labels[index]), style = ChipLabel)
                            .size.width.toFloat()
                        val hasIcon = token.spaceIcon != null ||
                            token.objectIcon != null || token.iconRes != null
                        with(density) {
                            pillsWidth += textWidth +
                                (if (hasIcon) 26.dp.toPx() else 0f) + // icon + gap
                                22.dp.toPx() // pill paddings + inter-pill spacing
                        }
                    }
                    // A usable field plus its paddings must survive.
                    val reserved = with(density) { 76.dp.toPx() }
                    pillsWidth > constraints.maxWidth - reserved
                }
            }
            val collapseAll = query.isNotEmpty() || overflows
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
            tokens.forEach { token ->
                TokenPill(
                    token = token,
                    isSelected = token.token.id == selectedTokenId,
                    collapseToIcon = collapseAll,
                    onClick = { onTokenClicked(token) },
                    onRemove = { onTokenRemoved(token) },
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            // Internal TextFieldValue keeps the caret stable across external
            // updates and recompositions (a plain String value resets it to 0).
            var fieldValue by remember {
                mutableStateOf(TextFieldValue(query, TextRange(query.length)))
            }
            if (fieldValue.text != query) {
                fieldValue = TextFieldValue(query, TextRange(query.length))
            }
            BasicTextField(
                value = fieldValue,
                onValueChange = { value ->
                    fieldValue = value
                    if (value.text != query) onQueryChanged(value.text)
                },
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .weight(1f, fill = true)
                    .focusRequester(focusRequester)
                    // Backspace in an empty input: first press selects the
                    // last pill, second press removes it.
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown &&
                            event.key == Key.Backspace &&
                            query.isEmpty() &&
                            tokens.isNotEmpty()
                        ) {
                            onBackspaceWhenEmpty()
                            true
                        } else {
                            false
                        }
                    },
                maxLines = 1,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                textStyle = BodyRegular.copy(color = colorResource(R.color.text_primary)),
                cursorBrush = SolidColor(colorResource(R.color.cursor_color)),
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(Loc.string.search_v2_hint),
                                style = BodyRegular,
                                color = colorResource(R.color.text_tertiary),
                                maxLines = 1
                            )
                        }
                        innerTextField()
                    }
                }
            )
            }
            if (focusOnStart) {
                // Inside the BoxWithConstraints subcomposition, so the
                // requester is attached before this runs; best-effort —
                // autofocus must never crash the screen.
                LaunchedEffect(Unit) {
                    runCatching { focusRequester.requestFocus() }
                }
            }
        }
        if (query.isNotEmpty()) {
            Icon(
                painter = painterResource(R.drawable.ic_clear_18),
                contentDescription = "Clear",
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onQueryChanged("") }
                    .padding(15.dp),
                tint = colorResource(R.color.glyph_active)
            )
        }
    }
}

@Composable
private fun TokenPill(
    token: TokenView,
    isSelected: Boolean,
    collapseToIcon: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasIcon = token.spaceIcon != null || token.objectIcon != null || token.iconRes != null
    val iconOnly = collapseToIcon && hasIcon && !isSelected
    val removeLabel = stringResource(Loc.string.remove)
    Row(
        modifier = modifier
            .height(28.dp)
            .background(
                color = if (isSelected) {
                    colorResource(R.color.control_accent)
                } else {
                    colorResource(R.color.shape_secondary)
                },
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            // There is no × on pills — tap-select + backspace is the pointer
            // path, so removal must exist as an explicit accessibility action.
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction(removeLabel) {
                        onRemove()
                        true
                    }
                )
            }
            .padding(horizontal = if (iconOnly) 4.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val spaceIcon = token.spaceIcon
        val objectIcon = token.objectIcon
        val iconRes = token.iconRes
        when {
            spaceIcon != null -> SpaceIconView(
                icon = spaceIcon,
                mainSize = 20.dp
            )
            objectIcon != null -> ListWidgetObjectIcon(
                icon = objectIcon,
                iconSize = 20.dp,
                modifier = Modifier
            )
            iconRes != null -> Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isSelected) {
                    colorResource(R.color.text_white)
                } else {
                    colorResource(R.color.glyph_active)
                }
            )
        }
        if (!iconOnly) {
            Text(
                text = tokenLabel(token),
                style = ChipLabel,
                color = if (isSelected) {
                    colorResource(R.color.text_white)
                } else {
                    colorResource(R.color.text_primary)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = if (hasIcon) 6.dp else 0.dp)
            )
        }
    }
}

@Composable
private fun tokenLabel(token: TokenView): String {
    val res = token.labelRes
    return when {
        // An unnamed space/chat must not render a blank pill.
        token.label != null -> token.label.orEmpty().orUntitled()
        res != null && token.formatArg != null -> stringResource(res, token.formatArg.orEmpty())
        res != null -> stringResource(res)
        else -> ""
    }
}

/** The vault's fallback for unnamed spaces/objects, applied at render time. */
@Composable
private fun String.orUntitled(): String =
    ifEmpty { stringResource(Loc.string.untitled) }

//endregion

//region Chips

@Composable
fun SearchChipsRow(
    chips: List<ChipView>,
    onChipClicked: (ChipView) -> Unit,
    modifier: Modifier = Modifier
) {
    if (chips.isEmpty()) return
    val scrollState = rememberScrollState()
    // A different chip set (e.g. the refinement package) starts at the front —
    // the row must not inherit the previous set's scroll offset.
    LaunchedEffect(chips.firstOrNull()?.id) {
        scrollState.scrollTo(0)
    }
    // A plain scrollable Row, deliberately not lazy: the set is ≤12 chips and
    // a lazy row re-composes chips during fling (iOS A.22).
    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chip ->
            SuggestionChip(chip = chip, onClick = { onChipClicked(chip) })
        }
    }
}

@Composable
private fun SuggestionChip(
    chip: ChipView,
    onClick: () -> Unit
) {
    val hasIcon = chip.spaceIcon != null || chip.objectIcon != null || chip.iconRes != null
    Row(
        modifier = Modifier
            .height(32.dp)
            .border(
                width = 1.dp,
                color = colorResource(R.color.shape_primary),
                shape = CircleShape
            )
            .clickable { onClick() }
            // iOS chip metrics: 10dp horizontal padding, 16dp leading icon,
            // 4dp icon-to-label gap. Text-only chips get +2dp optical
            // compensation at the start: flat capital stems against the
            // margin read tighter than the open letterforms on the right.
            .padding(start = if (hasIcon) 10.dp else 12.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val spaceIcon = chip.spaceIcon
        val objectIcon = chip.objectIcon
        val iconRes = chip.iconRes
        when {
            spaceIcon != null -> {
                SpaceIconView(icon = spaceIcon, mainSize = 16.dp)
                Spacer(modifier = Modifier.width(4.dp))
            }
            objectIcon != null -> {
                ListWidgetObjectIcon(icon = objectIcon, iconSize = 16.dp, modifier = Modifier)
                Spacer(modifier = Modifier.width(4.dp))
            }
            iconRes != null -> {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = colorResource(R.color.glyph_active)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
        val res = chip.labelRes
        val text = when {
            chip.label != null -> chip.label.orEmpty().orUntitled()
            res != null && chip.formatArg != null -> stringResource(res, chip.formatArg.orEmpty())
            res != null -> stringResource(res)
            else -> ""
        }
        Text(
            text = text,
            style = ChipLabel,
            color = colorResource(R.color.text_primary),
            maxLines = 1
        )
    }
}

//endregion

//region Pickers

@Composable
private fun PickerSheetContent(
    rows: List<PickerRowView>,
    onRowClicked: (PickerRowView) -> Unit
) {
    var pickerQuery by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxWidth()) {
        DefaultSearchBar(
            value = pickerQuery,
            onQueryChanged = { pickerQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        val filtered = remember(rows, pickerQuery) {
            rows.filter { row ->
                pickerQuery.isBlank() || row.label.contains(pickerQuery.trim(), ignoreCase = true)
            }
        }
        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Text(
                    text = stringResource(Loc.string.search_v2_no_results),
                    style = BodyRegular,
                    color = colorResource(R.color.text_secondary),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                items(count = filtered.size, key = { filtered[it].id }) { index ->
                    val row = filtered[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { onRowClicked(row) }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val spaceIcon = row.spaceIcon
                        val objectIcon = row.objectIcon
                        when {
                            spaceIcon != null -> SpaceIconView(icon = spaceIcon, mainSize = 40.dp)
                            objectIcon != null -> ListWidgetObjectIcon(
                                icon = objectIcon,
                                iconSize = 40.dp,
                                modifier = Modifier
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        ) {
                            Text(
                                text = row.label.orUntitled(),
                                style = PreviewTitle2Medium,
                                color = colorResource(R.color.text_primary),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val captionRes = row.captionRes
                            if (captionRes != null) {
                                Text(
                                    text = when {
                                        row.captionStringArg != null ->
                                            stringResource(captionRes, row.captionStringArg.orEmpty())
                                        row.captionCountArg != null ->
                                            stringResource(captionRes, row.captionCountArg ?: 0)
                                        else -> stringResource(captionRes)
                                    },
                                    style = Relations3,
                                    color = colorResource(R.color.text_secondary),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

//endregion

//region Results

@Composable
private fun SearchResultsList(
    state: com.anytypeio.anytype.feature_search.presentation.SearchUiState,
    viewModel: SearchViewModel,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Infinite scroll: request the next page when the end of the list shows.
    // One snapshotFlow over layout info — reads happen in the flow, not in
    // composition, so scrolling never recomposes this scope.
    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            last >= info.totalItemsCount - 3 && info.totalItemsCount > 0
        }
            .distinctUntilChanged()
            .collect { atEnd -> if (atEnd) viewModel.onLoadMore() }
    }

    // A fresh (non-append) load resets the scroll to the top — explicitly,
    // never by re-keying the whole list (iOS A.21).
    LaunchedEffect(state.loadEpoch) {
        if (state.results.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading && state.results.isEmpty() -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp),
                    color = colorResource(R.color.glyph_active),
                    strokeWidth = 2.dp
                )
            }
            state.isEmpty && !state.isLoading -> {
                Text(
                    text = stringResource(Loc.string.search_v2_no_results),
                    style = BodyRegular,
                    color = colorResource(R.color.text_secondary),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    // Results scroll behind the keyboard; the inset lives in
                    // contentPadding so the last row clears it. Keeping the
                    // IME inset off the root avoids remeasuring the whole
                    // screen on every keyboard animation frame.
                    contentPadding = WindowInsets.ime.asPaddingValues()
                ) {
                    items(
                        count = state.results.size,
                        key = { state.results[it].key },
                        // javaClass, not ::class — KClass lookups allocate a
                        // fresh ClassReference on every call during scroll.
                        contentType = { state.results[it].javaClass }
                    ) { index ->
                        when (val row = state.results[index]) {
                            is SearchResultView.SectionHeader -> SectionHeaderRow(
                                row = row,
                                onSortSelected = viewModel::onBrowseSortSelected
                            )
                            is SearchResultView.ChannelRow -> ChannelRow(
                                row = row,
                                onClick = { viewModel.onResultClicked(row) },
                                onDrill = { viewModel.onChannelDrill(row) }
                            )
                            is SearchResultView.SuggestionRow -> SuggestionRowItem(
                                row = row,
                                onClick = { viewModel.onResultClicked(row) }
                            )
                            is SearchResultView.FocusPersonSpaceRow -> FocusPersonSpaceRowItem(
                                row = row,
                                onClick = { viewModel.onResultClicked(row) }
                            )
                            SearchResultView.MessagesTutorialRow -> MessagesTutorialItem(
                                onClick = { viewModel.onResultClicked(row) }
                            )
                            SearchResultView.CreateChannelRow -> CreateChannelItem(
                                onClick = { viewModel.onResultClicked(row) }
                            )
                            is SearchResultView.PersonRow -> PersonRow(
                                row = row,
                                onClick = { viewModel.onResultClicked(row) },
                                onDrill = { viewModel.onPersonDrill(row) }
                            )
                            is SearchResultView.ObjectRow -> ObjectRow(
                                row = row,
                                onClick = { viewModel.onResultClicked(row) },
                                onDrill = { viewModel.onObjectDrill(row) }
                            )
                            is SearchResultView.MessageRow -> MessageRow(
                                row = row,
                                onClick = { viewModel.onResultClicked(row) }
                            )
                            is SearchResultView.TypeAggRow -> TypeAggRow(
                                row = row,
                                onClick = { viewModel.onResultClicked(row) },
                                onDrill = { viewModel.onTypeAggDrill(row) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeaderRow(
    row: SearchResultView.SectionHeader,
    onSortSelected: (BrowseSort) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val titleRes = row.titleRes
        Text(
            text = when {
                row.title != null -> row.title.orEmpty()
                titleRes != null && row.formatArg != null ->
                    stringResource(titleRes, row.formatArg.orEmpty())
                titleRes != null -> stringResource(titleRes)
                else -> ""
            },
            style = Caption1Medium,
            color = colorResource(R.color.text_secondary)
        )
        Spacer(modifier = Modifier.weight(1f))
        val sort = row.sortMenu
        if (sort != null) {
            Box {
                Text(
                    text = stringResource(
                        when (sort) {
                            BrowseSort.EDITED -> Loc.string.search_v2_recently_edited
                            BrowseSort.CREATED -> Loc.string.search_v2_recently_created
                            BrowseSort.NAME -> Loc.string.search_v2_sort_name
                        }
                    ),
                    style = Caption1Regular,
                    color = colorResource(R.color.text_tertiary),
                    modifier = Modifier.clickable { menuExpanded = true }
                )
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    // The default M3 menu surface ignores the app palette and
                    // renders as a bare white square in dark theme.
                    containerColor = colorResource(R.color.background_secondary)
                ) {
                    listOf(
                        BrowseSort.EDITED to Loc.string.search_v2_recently_edited,
                        BrowseSort.CREATED to Loc.string.search_v2_recently_created,
                        BrowseSort.NAME to Loc.string.search_v2_sort_name
                    ).forEach { (option, labelRes) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(labelRes),
                                    style = Caption1Regular,
                                    color = colorResource(R.color.text_primary)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onSortSelected(option)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelRow(
    row: SearchResultView.ChannelRow,
    onClick: () -> Unit,
    onDrill: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpaceIconView(icon = row.icon, mainSize = 40.dp)
        Text(
            text = row.name.orUntitled(),
            style = PreviewTitle2Medium,
            color = colorResource(R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        )
        DrillAffordance(row.name, onDrill)
    }
}

@Composable
private fun PersonRow(
    row: SearchResultView.PersonRow,
    onClick: () -> Unit,
    onDrill: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ListWidgetObjectIcon(icon = row.icon, iconSize = 40.dp, modifier = Modifier)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = row.name.orUntitled(),
                style = PreviewTitle2Medium,
                color = colorResource(R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                // Zero shared Channels reads as identity, not "0 Channels":
                // the global name, else a short identity prefix (iOS §8.3).
                text = when {
                    row.sharedChannelCount == 1 ->
                        stringResource(Loc.string.search_v2_person_member_in_one)
                    row.sharedChannelCount > 1 ->
                        stringResource(Loc.string.search_v2_person_member_in, row.sharedChannelCount)
                    !row.globalName.isNullOrBlank() -> row.globalName.orEmpty()
                    else -> row.identity.take(6) + "…"
                },
                style = Relations3,
                color = colorResource(R.color.text_secondary),
                maxLines = 1
            )
        }
        DrillAffordance(row.name, onDrill)
    }
}

@Composable
private fun ObjectRow(
    row: SearchResultView.ObjectRow,
    onClick: () -> Unit,
    onDrill: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ListWidgetObjectIcon(
            icon = row.icon,
            iconSize = 48.dp,
            modifier = Modifier
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = highlighted(row.name, row.nameHighlights),
                style = PreviewTitle2Medium,
                color = colorResource(R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val snippet = row.snippet
            if (!snippet.isNullOrBlank()) {
                Text(
                    text = highlighted(snippet, row.snippetHighlights),
                    style = Relations3,
                    color = colorResource(R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val typeName = row.typeName
                if (!typeName.isNullOrBlank()) {
                    Text(
                        text = typeName,
                        style = Relations3,
                        color = colorResource(R.color.text_secondary),
                        maxLines = 1
                    )
                }
                val spaceName = row.spaceName
                if (!spaceName.isNullOrBlank()) {
                    if (!typeName.isNullOrBlank()) {
                        Text(
                            text = " · ",
                            style = Relations3,
                            color = colorResource(R.color.text_secondary)
                        )
                    }
                    Text(
                        text = stringResource(Loc.string.search_v2_in_space_caption, spaceName),
                        style = Relations3,
                        color = colorResource(R.color.text_secondary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (row.drill != null) {
            DrillAffordance(row.name, onDrill)
        }
    }
}

@Composable
private fun MessageRow(
    row: SearchResultView.MessageRow,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        val authorIcon = row.authorIcon
        if (authorIcon != null) {
            ListWidgetObjectIcon(
                icon = authorIcon,
                iconSize = 40.dp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = row.authorName,
                    style = Caption1Medium,
                    color = colorResource(R.color.text_primary),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = remember(row.createdAt) { formatMessageDate(row.createdAt) },
                    style = Caption1Regular,
                    color = colorResource(R.color.text_tertiary),
                    maxLines = 1
                )
            }
            Text(
                text = highlighted(row.text, row.highlights),
                style = Relations3,
                color = colorResource(R.color.text_primary),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                val container = row.containerName
                if (!container.isNullOrBlank()) {
                    Text(
                        text = container,
                        style = Relations3,
                        color = colorResource(R.color.text_secondary),
                        maxLines = 1
                    )
                }
                val spaceName = row.spaceName
                if (!spaceName.isNullOrBlank()) {
                    if (!container.isNullOrBlank()) {
                        Text(
                            text = " · ",
                            style = Relations3,
                            color = colorResource(R.color.text_secondary)
                        )
                    }
                    Text(
                        text = stringResource(Loc.string.search_v2_in_space_caption, spaceName),
                        style = Relations3,
                        color = colorResource(R.color.text_secondary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun TypeAggRow(
    row: SearchResultView.TypeAggRow,
    onClick: () -> Unit,
    onDrill: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ListWidgetObjectIcon(icon = row.icon, iconSize = 40.dp, modifier = Modifier)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = row.name,
                style = PreviewTitle2Medium,
                color = colorResource(R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (row.spaceCount <= 1 && !row.spaceName.isNullOrBlank()) {
                    stringResource(Loc.string.search_v2_in_space_caption, row.spaceName.orEmpty())
                } else {
                    stringResource(Loc.string.search_v2_type_agg_caption, row.spaceCount)
                },
                style = Relations3,
                color = colorResource(R.color.text_secondary),
                maxLines = 1
            )
        }
        DrillAffordance(row.name, onDrill)
    }
}

@Composable
private fun SuggestionRowItem(
    row: SearchResultView.SuggestionRow,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_search_18),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = colorResource(R.color.glyph_active)
        )
        Text(
            text = if (row.formatArg != null) {
                stringResource(row.labelRes, row.formatArg.orEmpty())
            } else {
                stringResource(row.labelRes)
            },
            style = PreviewTitle2Medium,
            color = colorResource(R.color.control_accent),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun FocusPersonSpaceRowItem(
    row: SearchResultView.FocusPersonSpaceRow,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ListWidgetObjectIcon(icon = row.icon, iconSize = 40.dp, modifier = Modifier)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                // The person is the constant across these rows — the title;
                // the Channel varies, so it captions (iOS §9.2).
                text = row.personName.orUntitled(),
                style = PreviewTitle2Medium,
                color = colorResource(R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (row.isOneToOne) {
                    stringResource(Loc.string.search_v2_one_to_one_caption)
                } else {
                    stringResource(Loc.string.search_v2_in_space_caption, row.spaceName.orUntitled())
                },
                style = Relations3,
                color = colorResource(R.color.text_secondary),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CreateChannelItem(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_default_plus),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = colorResource(R.color.glyph_active)
        )
        Text(
            text = stringResource(Loc.string.search_v2_create_channel),
            style = PreviewTitle2Medium,
            color = colorResource(R.color.text_primary),
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun MessagesTutorialItem(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Loc.string.search_v2_no_results),
            style = BodyRegular,
            color = colorResource(R.color.text_secondary)
        )
        Text(
            text = stringResource(Loc.string.search_v2_messages_tutorial_hint),
            style = Caption1Regular,
            color = colorResource(R.color.text_tertiary),
            modifier = Modifier.padding(top = 12.dp)
        )
        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .height(36.dp)
                .border(
                    width = 1.dp,
                    color = colorResource(R.color.shape_primary),
                    shape = CircleShape
                )
                .clickable { onClick() }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_chat_32),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = colorResource(R.color.glyph_active)
            )
            Text(
                text = stringResource(Loc.string.search_v2_messages_tutorial_button),
                style = ChipLabel,
                color = colorResource(R.color.text_primary),
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

@Composable
private fun DrillAffordance(rowName: String, onDrill: () -> Unit) {
    // North-west "insert into query" arrow (the Google/YouTube search
    // suggestion idiom) — a chevron would read as "open detail" on Android.
    Icon(
        painter = painterResource(R.drawable.ic_arrow_insert_18),
        contentDescription = stringResource(Loc.string.search_v2_filter_by, rowName),
        modifier = Modifier
            .size(40.dp)
            .clickable { onDrill() }
            .padding(11.dp),
        tint = colorResource(R.color.glyph_active)
    )
}

@Composable
private fun highlighted(text: String, ranges: List<IntRange>): AnnotatedString {
    if (ranges.isEmpty()) return remember(text) { AnnotatedString(text) }
    val background = colorResource(R.color.palette_light_ice)
    return remember(text, ranges, background) { buildAnnotatedStringWith(text, ranges, background) }
}

private fun buildAnnotatedStringWith(
    text: String,
    ranges: List<IntRange>,
    background: androidx.compose.ui.graphics.Color
): AnnotatedString {
    return buildAnnotatedString {
        append(text)
        ranges.forEach { range ->
            val start = range.first.coerceIn(0, text.length)
            // The wire range's `to` is exclusive — every other consumer
            // (chat search, GlobalSearch) treats `last` as the end index.
            val end = range.last.coerceIn(start, text.length)
            if (end > start) {
                addStyle(SpanStyle(background = background), start, end)
            }
        }
    }
}

private fun formatMessageDate(timestampInSeconds: Long): String {
    return android.text.format.DateFormat.format(
        "d MMM",
        timestampInSeconds * 1000L
    ).toString()
}

//endregion
