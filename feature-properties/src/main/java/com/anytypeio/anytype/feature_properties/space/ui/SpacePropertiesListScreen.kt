package com.anytypeio.anytype.feature_properties.space.ui

import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.simpleIcon
import com.anytypeio.anytype.core_ui.extensions.swapList
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.UxSmallTextRegular
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.feature_properties.space.UiSpacePropertiesScreenState
import com.anytypeio.anytype.feature_properties.space.UiSpacePropertyItem

@Composable
fun SpacePropertiesListScreen(
    uiState: UiSpacePropertiesScreenState,
    onPropertyClicked: (UiSpacePropertyItem.Item) -> Unit,
    onBackPressed: () -> Unit,
    onAddIconClicked: () -> Unit,
    onMoveToBin: (UiSpacePropertyItem.Item) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.background_primary))
            .systemBarsPadding()
    ) {

        Topbar(
            onBackPressed = onBackPressed,
            onAddIconClicked = onAddIconClicked
        )

        val contentModifier =
            if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
                Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .fillMaxWidth()
            else
                Modifier
                    .fillMaxWidth()
        val lazyListState = rememberLazyListState()

        val items = remember {
            mutableStateListOf<UiSpacePropertyItem>()
        }
        items.swapList(uiState.items)

        LazyColumn(
            modifier = contentModifier,
            state = lazyListState,
        ) {
            items(
                count = items.size,
                key = { index -> items[index].id },
                itemContent = {
                    val item = items[it]
                    when (item ) {
                        is UiSpacePropertyItem.Item -> {
                            Property(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .padding(start = 20.dp, end = 20.dp),
                                item = item,
                                onPropertyClicked = onPropertyClicked,
                                onMoveToBin = onMoveToBin
                            )
                            Divider()
                        }
                        is UiSpacePropertyItem.Section -> {
                            Section(item)
                        }
                    }
                }
            )
            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                )
            }
        }
    }
}

@Composable
private fun Section(section: UiSpacePropertyItem.Section) {
    val text = when (section) {
        is UiSpacePropertyItem.Section.MyProperties ->
            stringResource(R.string.space_properties_screen_section_my_types)
        is UiSpacePropertyItem.Section.SystemProperties ->
            stringResource(R.string.space_properties_screen_section_system_types)
    }
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                modifier = Modifier
                    .padding(bottom = 8.dp, start = 20.dp)
                    .align(Alignment.BottomStart),
                text = text,
                style = Caption1Medium,
                color = colorResource(R.color.text_secondary),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Property(
    modifier: Modifier,
    item: UiSpacePropertyItem.Item,
    onPropertyClicked: (UiSpacePropertyItem.Item) -> Unit,
    onMoveToBin: (UiSpacePropertyItem.Item) -> Unit
) {
    val isMenuExpanded = remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onPropertyClicked(item) },
                onLongClick = { isMenuExpanded.value = true }
            ),
        verticalAlignment = CenterVertically
    ) {
        val icon = item.format.simpleIcon()
        PropertyIcon(
            modifier = Modifier.size(24.dp),
            formatIconRes = icon
        )
        val name = item.name.trim().ifBlank { stringResource(R.string.untitled) }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp),
            text = name,
            style = PreviewTitle1Regular,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (item.isPossibleMoveToBin) {
            ItemDropDownMenu(
                item = item,
                showMenu = isMenuExpanded.value,
                onDismissRequest = { isMenuExpanded.value = false },
                onMoveToBin = {
                    isMenuExpanded.value = false
                    onMoveToBin(it)
                }
            )
        }
    }
}

@Composable
fun ItemDropDownMenu(
    item: UiSpacePropertyItem.Item,
    showMenu: Boolean,
    onDismissRequest: () -> Unit,
    onMoveToBin: (UiSpacePropertyItem.Item) -> Unit
) {
    DropdownMenu(
        modifier = Modifier
            .width(244.dp),
        expanded = showMenu,
        offset = DpOffset(x = 0.dp, y = 0.dp),
        onDismissRequest = {
            onDismissRequest()
        },
        shape = RoundedCornerShape(10.dp),
        containerColor = colorResource(id = R.color.background_secondary),
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(R.string.space_properties_screen_menu_move_to_bin),
                    style = UxSmallTextRegular,
                    color = colorResource(id = R.color.text_primary)
                )
            },
            onClick = {
                onMoveToBin(item)
            },
        )
    }
}

@Composable
private fun RowScope.PropertyIcon(
    modifier: Modifier,
    formatIconRes: Int?
) {
    if (formatIconRes != null) {
        Image(
            painter = painterResource(id = formatIconRes),
            contentDescription = "Property format icon",
            contentScale = ContentScale.None,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Topbar(
    onBackPressed: () -> Unit,
    onAddIconClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(48.dp),
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .fillMaxHeight()
                .noRippleThrottledClickable {
                    onBackPressed()
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier
                    .wrapContentSize(),
                painter = painterResource(R.drawable.ic_default_top_back),
                contentDescription = stringResource(R.string.content_desc_back_button)
            )
        }
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(R.string.space_properties_screen_title),
            style = Title1,
            color = colorResource(R.color.text_primary),
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(48.dp)
                .align(Alignment.CenterEnd)
                .noRippleThrottledClickable {
                    onAddIconClicked()
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Image(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .wrapContentSize(),
                painter = painterResource(R.drawable.ic_default_plus),
                contentDescription = "Add new type"
            )
        }
    }
}