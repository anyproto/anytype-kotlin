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
import com.anytypeio.anytype.core_models.Id
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
    /** DROID-4397: add the widget's source object to personal favorites. */
    data class FavoriteObject(val widgetId: WidgetId) : WidgetMenuItem()
    /** DROID-4397: remove the widget's source object from personal favorites. */
    data class UnfavoriteObject(val widgetId: WidgetId) : WidgetMenuItem()
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
                is WidgetMenuItem.FavoriteObject -> {
                    DropdownMenuItem(
                        onClick = {
                            onDropDownMenuAction(DropDownMenuAction.FavoriteObject(menuItem.widgetId)).also {
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
                                    text = stringResource(R.string.favourite)
                                )
                                Image(
                                    painter = painterResource(
                                        id = R.drawable.ic_object_action_add_to_favorites
                                    ),
                                    contentDescription = "Favorite icon",
                                    modifier = Modifier.size(24.dp),
                                    colorFilter = ColorFilter.tint(
                                        colorResource(id = R.color.text_primary)
                                    )
                                )
                            }
                        }
                    )
                    if (index < menuItems.lastIndex) {
                        Divider(
                            thickness = 0.5.dp,
                            color = colorResource(id = R.color.shape_primary)
                        )
                    }
                }
                is WidgetMenuItem.UnfavoriteObject -> {
                    DropdownMenuItem(
                        onClick = {
                            onDropDownMenuAction(DropDownMenuAction.UnfavoriteObject(menuItem.widgetId)).also {
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
                                    text = stringResource(R.string.unfavorite)
                                )
                                // TODO(DROID-4397): Spec calls for a filled-star icon.
                                // Reusing the strikethrough-unfavorite asset for now.
                                Image(
                                    painter = painterResource(
                                        id = R.drawable.ic_object_action_unfavorite
                                    ),
                                    contentDescription = "Unfavorite icon",
                                    modifier = Modifier.size(24.dp),
                                    colorFilter = ColorFilter.tint(
                                        colorResource(id = R.color.text_primary)
                                    )
                                )
                            }
                        }
                    )
                    if (index < menuItems.lastIndex) {
                        Divider(
                            thickness = 0.5.dp,
                            color = colorResource(id = R.color.shape_primary)
                        )
                    }
                }
            }
        }
    }
}

/**
 * DROID-4397: emit a Favorite or Unfavorite menu item for [widget] iff its
 * source is a concrete real-object link (not a bundled view). Bundled widget
 * sources (`favorite`, `recent`, `bin`, `allObjects`, `chat`, `personalFavorites`)
 * aren't individual objects and have no meaningful favorite toggle.
 */
private fun MutableList<WidgetMenuItem>.addFavoriteToggleIfObjectBacked(
    widget: WidgetView,
    favoriteTargets: Set<Id>
) {
    val source: Widget.Source? = when (widget) {
        is WidgetView.Link -> widget.source
        is WidgetView.Tree -> widget.source
        is WidgetView.SetOfObjects -> widget.source
        is WidgetView.ListOfObjects -> widget.source
        is WidgetView.Gallery -> widget.source
        is WidgetView.ChatList -> widget.source
        else -> null
    }
    if (source == null || source is Widget.Source.Bundled || source is Widget.Source.Other) return
    if (source.id in favoriteTargets) {
        add(WidgetMenuItem.UnfavoriteObject(widget.id))
    } else {
        add(WidgetMenuItem.FavoriteObject(widget.id))
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
 *
 * @param canToggleChannelPin DROID-4397: when false, hides [RemoveWidget] for
 *  widgets in the PINNED section (since RemoveWidget = "Unpin from channel",
 *  which is Owner/Admin only per spec). Defaults to true so legacy call sites
 *  keep existing behavior.
 * @param favoriteTargets DROID-4397: set of object IDs currently in the user's
 *  personal favorites. If the widget's source is a concrete object (non-bundled)
 *  whose ID is in the set, the Unfavorite item is emitted; otherwise Favorite.
 *  Bundled widgets (Favorites, Recent, Bin, All Objects, etc.) don't have a
 *  real object and get no favorite/unfavorite item. Defaults to [emptySet] so
 *  legacy call sites keep existing behavior (no favorite toggle).
 */
fun WidgetView.getWidgetMenuItems(
    canToggleChannelPin: Boolean = true,
    favoriteTargets: Set<Id> = emptySet()
): List<WidgetMenuItem> {
    val menuItems = when (sectionType) {
        SectionType.UNREAD -> {
            // Unread section widgets have no menu
            emptyList<WidgetMenuItem>()
        }
        SectionType.PINNED -> {
            buildList {
                // DROID-4397: favorite/unfavorite the underlying object,
                // available to all roles, for any pinned widget whose source
                // is a concrete real object. Pin is implicit (widget already
                // exists in the shared pinned doc).
                addFavoriteToggleIfObjectBacked(this@getWidgetMenuItems, favoriteTargets)
                when (this@getWidgetMenuItems) {
                    is WidgetView.AllContent -> {
                        if (canToggleChannelPin) add(WidgetMenuItem.RemoveWidget)
                    }
                    is WidgetView.Bin -> {
                        if (canChangeWidgetType()) {
                            add(WidgetMenuItem.ChangeWidgetType)
                        }
                        if (canToggleChannelPin) add(WidgetMenuItem.RemoveWidget)
                    }
                    is WidgetView.ChatList -> {
                        if (canCreateObjectOfType) {
                            add(CreateObjectOfType(id))
                        }
                        if (canChangeWidgetType()) {
                            add(WidgetMenuItem.ChangeWidgetType)
                        }
                        if (canToggleChannelPin) add(WidgetMenuItem.RemoveWidget)
                    }
                    WidgetView.EmptyState -> {}
                    is WidgetView.Gallery -> {
                        if (canCreateObjectOfType) {
                            add(CreateObjectOfType(id))
                        }
                        if (canChangeWidgetType()) {
                            add(WidgetMenuItem.ChangeWidgetType)
                        }
                        if (canToggleChannelPin) add(WidgetMenuItem.RemoveWidget)
                    }
                    is WidgetView.Link -> {
                        if (canChangeWidgetType()) {
                            add(WidgetMenuItem.ChangeWidgetType)
                        }
                        if (canToggleChannelPin) add(WidgetMenuItem.RemoveWidget)
                    }
                    is WidgetView.ListOfObjects -> {
                        if (canCreateObjectOfType) {
                            add(CreateObjectOfType(id))
                        }
                        if (canChangeWidgetType()) {
                            add(WidgetMenuItem.ChangeWidgetType)
                        }
                        if (canToggleChannelPin) add(WidgetMenuItem.RemoveWidget)
                    }
                    is WidgetView.SetOfObjects -> {
                        if (canCreateObjectOfType) {
                            add(CreateObjectOfType(id))
                        }
                        if (canChangeWidgetType()) {
                            add(WidgetMenuItem.ChangeWidgetType)
                        }
                        if (canToggleChannelPin) add(WidgetMenuItem.RemoveWidget)
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
                        if (canToggleChannelPin) add(WidgetMenuItem.RemoveWidget)
                    }
                    is WidgetView.ObjectTypesGroup -> {
                        // Object types group has no menu - interactions are per-row
                    }
                    is WidgetView.RecentlyEdited -> {
                        // Recently edited has no menu
                    }
                    is WidgetView.InviteMembers -> {
                        // No menu for temporary widgets
                    }
                    is WidgetView.Home -> {
                        // Home widget uses its own menu composable (HomeWidgetMenu).
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
        SectionType.RECENTLY_EDITED -> {
            // Recently edited widgets menu behavior - TODO: Define menu items
            emptyList<WidgetMenuItem>()
        }
        SectionType.MY_FAVORITES -> {
            // Personal Favorites widgets have no menu - favorites are managed via the star toggle
            emptyList<WidgetMenuItem>()
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