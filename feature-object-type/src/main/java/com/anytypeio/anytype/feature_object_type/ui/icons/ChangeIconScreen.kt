package com.anytypeio.anytype.feature_object_type.ui.icons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.common.ReorderHapticFeedback
import com.anytypeio.anytype.core_ui.common.ReorderHapticFeedbackType
import com.anytypeio.anytype.core_ui.common.rememberReorderHapticFeedback
import com.anytypeio.anytype.core_ui.extensions.colorRes
import com.anytypeio.anytype.core_ui.foundation.DefaultSearchBar
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons.CustomIcons
import com.anytypeio.anytype.feature_object_type.R
import com.anytypeio.anytype.feature_object_type.ui.icons.ChangeIconScreenConst.secondRowColors
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeIconScreen(
    modifier: Modifier,
    onDismissRequest: () -> Unit,
    onIconClicked: (String, CustomIconColor?) -> Unit,
    onRemoveIconClicked: () -> Unit
) {

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val allIconNames = remember { CustomIcons.iconsMap.keys.toList() }

    ModalBottomSheet(
        modifier = modifier,
        dragHandle = {
            Column {
                Spacer(modifier = Modifier.height(6.dp))
                Dragger()
                Spacer(modifier = Modifier.height(6.dp))
            }
        },
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        scrimColor = colorResource(id = R.color.modal_screen_outside_background),
        containerColor = colorResource(id = R.color.background_secondary),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetState = bottomSheetState,
        onDismissRequest = onDismissRequest
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .statusBarsPadding()
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(R.string.object_type_icon_change_title),
                style = Title1,
                color = colorResource(R.color.text_primary),
                textAlign = TextAlign.Center
            )

            Text(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .noRippleThrottledClickable {
                        onRemoveIconClicked()
                    },
                text = stringResource(R.string.object_type_icon_remove),
                style = BodyRegular,
                color = colorResource(R.color.palette_system_red),
                textAlign = TextAlign.Center
            )
        }
        var searchQuery by remember { mutableStateOf("") }

        DefaultSearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            hint = R.string.object_type_icon_change_title_search_hint
        ) { newQuery ->
            searchQuery = newQuery
        }

        Divider(paddingStart = 0.dp, paddingEnd = 0.dp)

        Spacer(modifier = Modifier.height(16.dp))

        val filteredIcons = if (searchQuery.isEmpty()) {
            allIconNames
        } else {
            allIconNames.filter { it.contains(searchQuery, ignoreCase = true) }
        }

        IconSelectionGrid(
            icons = filteredIcons,
            onIconClicked = onIconClicked,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
fun IconSelectionGrid(
    modifier: Modifier = Modifier,
    icons: List<String>,
    onIconClicked: (String, CustomIconColor?) -> Unit
) {

    val hapticFeedback = rememberReorderHapticFeedback()

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(minSize = 57.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = icons,
            key = { iconName -> iconName },
            contentType = { "icon" }
        ) { iconName ->
            IconItem(
                modifier = Modifier.wrapContentSize(),
                hapticFeedback = hapticFeedback,
                iconName = iconName,
                onIconClicked = onIconClicked
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IconItem(
    modifier: Modifier,
    iconName: String,
    hapticFeedback: ReorderHapticFeedback,
    onIconClicked: (String, CustomIconColor?) -> Unit
) {
    val showIconPreviews = remember { mutableStateOf(false) }
    Box(modifier = modifier
        .combinedClickable(
            enabled = true,
            onClick = {
                onIconClicked(iconName, null)
            },
            onLongClick = {
                hapticFeedback.performHapticFeedback(ReorderHapticFeedbackType.START)
                showIconPreviews.value = true
            }
        )) {
        val imageVector = CustomIcons.getImageVector(iconName)
        val tintColor = if (!showIconPreviews.value) {
            colorResource(id = CustomIconColor.Gray.colorRes())
        } else {
            colorResource(id = R.color.glyph_inactive)
        }
        if (imageVector != null) {
            Image(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Center),
                imageVector = imageVector,
                contentDescription = "Object Type icon",
                colorFilter = ColorFilter.tint(tintColor),
            )
            IconPreviews(
                imageVector = imageVector,
                show = showIconPreviews.value,
                onDismissRequest = { showIconPreviews.value = false },
                onIconClicked = { color ->
                    showIconPreviews.value = false
                    onIconClicked(iconName, color)
                }
            )
        }
    }
}

@Composable
private fun IconPreviews(
    imageVector: ImageVector,
    show: Boolean,
    onDismissRequest: () -> Unit,
    onIconClicked: (CustomIconColor) -> Unit
) {
    DropdownMenu(
        modifier = Modifier
            .wrapContentSize()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        expanded = show,
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(size = 20.dp),
        containerColor = colorResource(id = R.color.background_primary),
        shadowElevation = 5.dp,
        border = BorderStroke(
            width = 0.5.dp,
            color = colorResource(id = R.color.background_secondary)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChangeIconScreenConst.firstRowColors.forEach { customColor ->
                val color = colorResource(id = customColor.colorRes())
                Image(
                    modifier = Modifier
                        .size(40.dp)
                        .noRippleThrottledClickable {
                            onIconClicked(customColor)
                        },
                    imageVector = imageVector,
                    contentDescription = "Object Type icon",
                    colorFilter = ColorFilter.tint(color),
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            secondRowColors.forEach { customColor ->
                val color = colorResource(id = customColor.colorRes())
                Image(
                    modifier = Modifier
                        .size(40.dp)
                        .noRippleThrottledClickable {
                            onIconClicked(customColor)
                        },
                    imageVector = imageVector,
                    contentDescription = "Object Type icon",
                    colorFilter = ColorFilter.tint(color),
                )
            }
        }
    }
}

object ChangeIconScreenConst {
    val firstRowColors = listOf(
        CustomIconColor.Gray,
        CustomIconColor.Yellow,
        CustomIconColor.Amber,
        CustomIconColor.Red,
        CustomIconColor.Pink
    )

    val secondRowColors = listOf(
        CustomIconColor.Purple,
        CustomIconColor.Blue,
        CustomIconColor.Sky,
        CustomIconColor.Teal,
        CustomIconColor.Green
    )
}

@Composable
@DefaultPreviews
fun DefaultChangeIconScreenPreview() {
    IconSelectionGrid(
        modifier = Modifier.fillMaxSize(),
        icons = CustomIcons.iconsMap.keys.toList(),
        onIconClicked = { _, _ -> },
    )
}