package com.anytypeio.anytype.ui.widgets.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.SectionType
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.WidgetId
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.ui.widgets.menu.WidgetMenuItem.*

/**
 * Represents a menu item that can be displayed in the widget long-click menu.
 */
sealed class WidgetMenuItem {
    data class CreateObjectOfType(val widgetId: WidgetId) : WidgetMenuItem()
    data object ChangeWidgetType : WidgetMenuItem()
    data object RemoveWidget : WidgetMenuItem()
}

@Composable
fun WidgetLongClickMenu(
    menuItems: List<WidgetMenuItem>,
    isCardMenuExpanded: MutableState<Boolean>,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit
) {
    if (menuItems.isEmpty()) {
        // No menu to show, ensure it's closed
        if (isCardMenuExpanded.value) {
            isCardMenuExpanded.value = false
        }
        return
    }

    DropdownMenu(
        modifier = Modifier.width(254.dp),
        expanded = isCardMenuExpanded.value,
        onDismissRequest = { isCardMenuExpanded.value = false },
        containerColor = colorResource(R.color.background_secondary),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 8.dp,
        offset = DpOffset(
            x = 16.dp,
            y = 8.dp
        )
    ) {
        menuItems.forEachIndexed { index, menuItem ->
            when (menuItem) {
                is WidgetMenuItem.CreateObjectOfType -> {
                    DropdownMenuItem(
                        onClick = {
                            onDropDownMenuAction(DropDownMenuAction.CreateObjectOfType(menuItem.widgetId)).also {
                                isCardMenuExpanded.value = false
                            }
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    style = BodyRegular,
                                    color = colorResource(id = R.color.text_primary),
                                    text = stringResource(R.string.widgets_menu_new_object_type)
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.ic_menu_item_create),
                                    contentDescription = "New object icon",
                                    modifier = Modifier.size(24.dp),
                                    colorFilter = ColorFilter.tint(
                                        colorResource(id = R.color.text_primary)
                                    )
                                )
                            }
                        }
                    )
                    // Add thick divider after "Create Object" if not the last item
                    if (index < menuItems.lastIndex) {
                        Divider(
                            thickness = 8.dp,
                            color = colorResource(id = R.color.shape_primary)
                        )
                    }
                }
                is WidgetMenuItem.ChangeWidgetType -> {
                    DropdownMenuItem(
                        onClick = {
                            onDropDownMenuAction(DropDownMenuAction.ChangeWidgetType).also {
                                isCardMenuExpanded.value = false
                            }
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    style = BodyRegular,
                                    color = colorResource(id = R.color.text_primary),
                                    text = stringResource(R.string.widget_change_type)
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.ic_menu_item_change_type),
                                    contentDescription = "Change Widget Type icon",
                                    modifier = Modifier.size(24.dp),
                                    colorFilter = ColorFilter.tint(
                                        colorResource(id = R.color.text_primary)
                                    )
                                )
                            }
                        }
                    )
                    // Add thin divider after "Change Type" if not the last item
                    if (index < menuItems.lastIndex) {
                        Divider(
                            thickness = 0.5.dp,
                            color = colorResource(id = R.color.shape_primary)
                        )
                    }
                }
                is WidgetMenuItem.RemoveWidget -> {
                    // Add thin divider before "Remove/Unpin" if not the first item
                    if (index > 0) {
                        Divider(
                            thickness = 0.5.dp,
                            color = colorResource(id = R.color.shape_primary)
                        )
                    }
                    DropdownMenuItem(
                        onClick = {
                            onDropDownMenuAction(DropDownMenuAction.RemoveWidget).also {
                                isCardMenuExpanded.value = false
                            }
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    style = BodyRegular,
                                    color = colorResource(id = R.color.text_primary),
                                    text = stringResource(R.string.widget_unpin)
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.ic_unpin_24),
                                    contentDescription = "Unpin widget icon",
                                    modifier = Modifier.size(24.dp),
                                    colorFilter = ColorFilter.tint(
                                        colorResource(id = R.color.text_primary)
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun WidgetView.canChangeWidgetType(): Boolean {
    return when (this) {
        is WidgetView.ChatList -> {
            val source = this.source
            return source !is Widget.Source.Bundled
        }
        is WidgetView.Gallery -> {
            val source = this.source
            return source !is Widget.Source.Bundled
        }
        is WidgetView.Link -> {
            val source = this.source
            return source !is Widget.Source.Bundled
        }
        is WidgetView.ListOfObjects -> {
            val source = this.source
            return source !is Widget.Source.Bundled
        }
        is WidgetView.SetOfObjects -> {
            val source = this.source
            return source !is Widget.Source.Bundled
        }
        is WidgetView.Tree -> {
            val source = this.source
            return source !is Widget.Source.Bundled
        }
        else -> false
    }
}

/**
 * Determines which menu items should be shown for this widget.
 * Returns a list of menu items, or an empty list if no menu should be displayed.
 */
fun WidgetView.getWidgetMenuItems(): List<WidgetMenuItem> {
    val menuItems = when (sectionType) {
        SectionType.UNREAD -> {
            // Unread section widgets have no menu
            emptyList<WidgetMenuItem>()
        }
        SectionType.PINNED -> {
            buildList {
                when (this@getWidgetMenuItems) {
                    is WidgetView.AllContent -> {
                        add(WidgetMenuItem.RemoveWidget)
                    }
                    is WidgetView.Bin -> {
                        if (canChangeWidgetType()) {
                            add(WidgetMenuItem.ChangeWidgetType)
                        }
                        add(WidgetMenuItem.RemoveWidget)
                    }
                    is WidgetView.ChatList -> {
                        if (canCreateObjectOfType) {
                            add(CreateObjectOfType(id))
                        }
                        if (canChangeWidgetType()) {
                            add(WidgetMenuItem.ChangeWidgetType)
                        }
                        add(WidgetMenuItem.RemoveWidget)
                    }
                    WidgetView.EmptyState -> {}
                    is WidgetView.Gallery -> {
                        if (canCreateObjectOfType) {
                            add(CreateObjectOfType(id))
                        }
                        if (canChangeWidgetType()) {
                            add(WidgetMenuItem.ChangeWidgetType)
                        }
                        add(WidgetMenuItem.RemoveWidget)
                    }
                    is WidgetView.Link -> {
                        if (canChangeWidgetType()) {
                            add(WidgetMenuItem.ChangeWidgetType)
                        }
                        add(WidgetMenuItem.RemoveWidget)
                    }
                    is WidgetView.ListOfObjects -> {
                        if (canCreateObjectOfType) {
                            add(CreateObjectOfType(id))
                        }
                        if (canChangeWidgetType()) {
                            add(WidgetMenuItem.ChangeWidgetType)
                        }
                        add(WidgetMenuItem.RemoveWidget)
                    }
                    is WidgetView.SetOfObjects -> {
                        if (canCreateObjectOfType) {
                            add(CreateObjectOfType(id))
                        }
                        if (canChangeWidgetType()) {
                            add(WidgetMenuItem.ChangeWidgetType)
                        }
                        add(WidgetMenuItem.RemoveWidget)
                    }
                    is WidgetView.SpaceChat -> {}
                    is WidgetView.UnreadChatList -> {
                        // No menu for unread chat list
                    }
                    is WidgetView.Tree -> {
                        if (canCreateObjectOfType) {
                            add(CreateObjectOfType(id))
                        }
                        if (canChangeWidgetType()) {
                            add(WidgetMenuItem.ChangeWidgetType)
                        }
                        add(WidgetMenuItem.RemoveWidget)
                    }
                }
            }
        }
        SectionType.TYPES -> {
            buildList {
                // TYPES widgets only show menu if they can create objects
                if (canCreateObjectOfType) {
                    add(CreateObjectOfType(id))
                }
            }
        }
        null -> {
            // No section type means no menu
            emptyList<WidgetMenuItem>()
        }

        SectionType.NONE -> {
            // No section means no menu
            emptyList<WidgetMenuItem>()
        }
    }
    return menuItems
}

@Composable
fun BinWidgetMenu(
    isCardMenuExpanded: MutableState<Boolean>,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit
) {
    DropdownMenu(
        modifier = Modifier.width(254.dp),
        expanded = isCardMenuExpanded.value,
        onDismissRequest = { isCardMenuExpanded.value = false },
        containerColor = colorResource(R.color.background_secondary),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 8.dp,
        offset = DpOffset(
            x = 16.dp,
            y = 8.dp
        )
    ) {
        DropdownMenuItem(
            onClick = {
                onDropDownMenuAction(DropDownMenuAction.EmptyBin).also {
                    isCardMenuExpanded.value = false
                }
            },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material.Text(
                        modifier = Modifier.weight(1f),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary),
                        text = stringResource(R.string.widget_empty_bin)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_widget_bin),
                        contentDescription = "Empty bin icon",
                        modifier = Modifier
                            .wrapContentSize(),
                        colorFilter = ColorFilter.tint(
                            colorResource(id = R.color.palette_system_red)
                        )
                    )
                }
            }
        )
    }
}

// Preview Helper Components
@DefaultPreviews
@Composable
fun WidgetLongClickMenuPreview_PinnedSection_WithCreateOption() {
    val isExpanded = remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray.copy(alpha = 0.1f))
    ) {
        WidgetLongClickMenu(
            isCardMenuExpanded = isExpanded,
            onDropDownMenuAction = { /* Preview action */ },
            menuItems = emptyList()
        )
    }
}