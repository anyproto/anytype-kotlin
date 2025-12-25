package com.anytypeio.anytype.core_ui.foundation.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DEFAULT_DISABLED_ALPHA
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.common.FULL_ALPHA
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleCombinedClickable
import com.anytypeio.anytype.presentation.navigation.NavPanelState

@Composable
fun BottomNavigationMenu(
    state: NavPanelState,
    modifier: Modifier = Modifier,
    onShareButtonClicked: () -> Unit = {},
    onHomeButtonClicked: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onAddDocClick: () -> Unit = {},
    onAddDocLongClick: () -> Unit = {}
) {
    // Determine create-button enabled & alpha based on state
    val (createEnabled, createAlpha) = when (state) {
        is NavPanelState.Default -> {
            val enabled = state.isCreateEnabled
            enabled to if (enabled) FULL_ALPHA else DEFAULT_DISABLED_ALPHA
        }

        is NavPanelState.Chat -> {
            val enabled = state.isCreateEnabled
            enabled to if (enabled) FULL_ALPHA else DEFAULT_DISABLED_ALPHA
        }

        NavPanelState.Init -> false to DEFAULT_DISABLED_ALPHA
    }

    // Build the left button item according to NavPanelState
    val leftNavItem = when (state) {
        is NavPanelState.Chat -> state.left.toNavItem(
            onShare = onShareButtonClicked,
            onChat = onHomeButtonClicked
        )

        is NavPanelState.Default -> state.left.toNavItem(
            onShare = onShareButtonClicked,
            onHome = onHomeButtonClicked
        )

        NavPanelState.Init -> NavItem(
            res = BottomNavigationItem.MEMBERS.res,
            contentDescRes = R.string.main_navigation_content_desc_members_button,
            onClick = onShareButtonClicked
        )
    }

    // Assemble all menu items
    val items = listOfNotNull(
        leftNavItem,
        NavItem(
            res = BottomNavigationItem.ADD_DOC.res,
            contentDescRes = R.string.main_navigation_content_desc_create_button,
            onClick = onAddDocClick,
            onLongClick = onAddDocLongClick,
            enabled = createEnabled,
            alpha = createAlpha
        ),
        NavItem(
            res = BottomNavigationItem.SEARCH.res,
            contentDescRes = R.string.main_navigation_content_desc_search_button,
            onClick = onSearchClick
        )
    )

    // Use wrapped width for 2 items (one-to-one spaces), fixed width for 3 items
    val widthModifier = if (items.size == 2) {
        Modifier.wrapContentWidth()
    } else {
        Modifier.width(BottomNavigationDefaults.Width)
    }

    Row(
        modifier = modifier
            .then(widthModifier)
            .height(BottomNavigationDefaults.Height)
            .background(
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.home_screen_toolbar_button)
            )
            .noRippleClickable { /* swallow underlying clicks */ },
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val itemModifier = if (items.size == 2) {
                // Fixed size for 2 items to allow proper wrapping
                Modifier
                    .width(BottomNavigationDefaults.ButtonWidth)
                    .fillMaxHeight()
                    .alpha(item.alpha)
            } else {
                // Weight for 3 items to fill the fixed container width
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .alpha(item.alpha)
            }
            MenuItem(
                modifier = itemModifier,
                contentDescription = stringResource(id = item.contentDescRes),
                res = item.res,
                onClick = item.onClick,
                onLongClick = item.onLongClick,
                enabled = item.enabled
            )
        }
    }
}

// Extension to map LeftButtonState to NavItem
private fun NavPanelState.LeftButtonState.toNavItem(
    onShare: () -> Unit,
    onHome: () -> Unit = {},
    onChat: () -> Unit = {}
): NavItem? = when (this) {
    is NavPanelState.LeftButtonState.AddMembers -> NavItem(
        res = BottomNavigationItem.ADD_MEMBERS.res,
        contentDescRes = R.string.main_navigation_content_desc_members_button,
        onClick = onShare,
        enabled = isActive,
        alpha = if (isActive) FULL_ALPHA else DEFAULT_DISABLED_ALPHA
    )

    NavPanelState.LeftButtonState.ViewMembers -> NavItem(
        res = BottomNavigationItem.MEMBERS.res,
        contentDescRes = R.string.main_navigation_content_desc_members_button,
        onClick = onShare
    )

    is NavPanelState.LeftButtonState.Home -> NavItem(
        res = BottomNavigationItem.HOME.res,
        contentDescRes = R.string.main_navigation_content_desc_home_button,
        onClick = onHome
    )

    NavPanelState.LeftButtonState.Chat -> NavItem(
        res = BottomNavigationItem.CHAT.res,
        contentDescRes = R.string.main_navigation_content_desc_chat_button,
        onClick = onChat
    )

    NavPanelState.LeftButtonState.Hidden -> null
}

// Data holder for menu items
private data class NavItem(
    @DrawableRes val res: Int,
    @StringRes val contentDescRes: Int,
    val onClick: () -> Unit = {},
    val onLongClick: () -> Unit = {},
    val enabled: Boolean = true,
    val alpha: Float = if (enabled) FULL_ALPHA else DEFAULT_DISABLED_ALPHA
)

@Composable
private fun MenuItem(
    modifier: Modifier,
    contentDescription: String,
    @DrawableRes res: Int,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    Image(
        painter = painterResource(id = res),
        contentDescription = contentDescription,
        contentScale = ContentScale.Inside,
        modifier = modifier
            .then(
                if (enabled) {
                    Modifier
                        .noRippleCombinedClickable(
                            onClick = onClick,
                            onLongClicked = {
                                haptic.performHapticFeedback(
                                    HapticFeedbackType.LongPress
                                )
                                onLongClick()
                            }
                        )
                } else {
                    Modifier
                }
            )
    )
}

private enum class BottomNavigationItem(@DrawableRes val res: Int) {
    HOME(R.drawable.ic_nav_panel_home),
    CHAT(R.drawable.ic_chat_32),
    MEMBERS(R.drawable.ic_nav_panel_members),
    ADD_MEMBERS(R.drawable.ic_nav_panel_add_member),
    SEARCH(R.drawable.ic_nav_panel_search),
    ADD_DOC(R.drawable.ic_nav_panel_plus)
}

@Immutable
object BottomNavigationDefaults {
    val Height = 52.dp
    val Width = 288.dp
    val ButtonWidth = 96.dp
}

@DefaultPreviews
@Composable
private fun NavBarPreviewOwner() {
    BottomNavigationMenu(
        onSearchClick = {},
        onAddDocClick = {},
        onAddDocLongClick = {},
        state = NavPanelState.Default(
            isCreateEnabled = true,
            left = NavPanelState.LeftButtonState.AddMembers(
                isActive = true
            )
        ),
        onHomeButtonClicked = {}
    )
}

@DefaultPreviews
@Composable
private fun NavBarPreviewReader() {
    BottomNavigationMenu(
        onSearchClick = {},
        onAddDocClick = {},
        onAddDocLongClick = {},
        state = NavPanelState.Default(
            isCreateEnabled = false,
            left = NavPanelState.LeftButtonState.ViewMembers
        ),
        onHomeButtonClicked = {}
    )
}