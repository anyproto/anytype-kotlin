package com.anytypeio.anytype.core_ui.foundation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DEFAULT_DISABLED_ALPHA
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.common.FULL_ALPHA
import com.anytypeio.anytype.core_ui.foundation.components.BottomNavigationDefaults.Height
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleCombinedClickable
import com.anytypeio.anytype.presentation.navigation.NavPanelState
import com.anytypeio.anytype.presentation.profile.ProfileIconView

@DefaultPreviews
@Composable
private fun NavBarPreviewOwner() {
    BottomNavigationMenu(
        searchClick = {},
        addDocClick = {},
        addDocLongClick = {},
        state = NavPanelState.Default(
            isCreateObjectButtonEnabled = true,
            leftButtonState = NavPanelState.LeftButtonState.AddMembers(
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
        searchClick = {},
        addDocClick = {},
        addDocLongClick = {},
        state = NavPanelState.Default(
            isCreateObjectButtonEnabled = false,
            leftButtonState = NavPanelState.LeftButtonState.ViewMembers
        ),
        onHomeButtonClicked = {}
    )
}


@DefaultPreviews
@Composable
private fun MyBottomNavigationMenu() {
    BottomNavigationMenu(
        searchClick = {},
        addDocClick = {},
        addDocLongClick = {},
        isOwnerOrEditor = true
    )
}

@DefaultPreviews
@Composable
private fun MyBottomViewerNavigationMenu() {
    BottomNavigationMenu(
        searchClick = {},
        addDocClick = {},
        addDocLongClick = {},
        isOwnerOrEditor = false
    )
}

@Composable
fun BottomNavigationMenu(
    modifier: Modifier = Modifier,
    onShareButtonClicked: () -> Unit = {},
    searchClick: () -> Unit = {},
    addDocClick: () -> Unit = {},
    addDocLongClick: () -> Unit = {},
    isOwnerOrEditor: Boolean
) {
    Row(
        modifier = modifier
            .height(Height)
            .wrapContentWidth()
            .background(
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.home_screen_toolbar_button)
            )
            /**
             * Workaround for clicks through the bottom navigation menu.
             */
            .noRippleClickable { },
        verticalAlignment = Alignment.CenterVertically
    ) {
        MenuItem(
            modifier = Modifier
                .width(72.dp)
                .height(52.dp),
            contentDescription = stringResource(id = R.string.main_navigation_content_desc_members_button),
            res = if (isOwnerOrEditor)
                BottomNavigationItem.ADD_MEMBERS.res
            else
                BottomNavigationItem.MEMBERS.res,
            onClick = onShareButtonClicked
        )
        MenuItem(
            modifier = Modifier
                .width(72.dp)
                .height(52.dp),
            contentDescription = stringResource(id = R.string.main_navigation_content_desc_search_button),
            res = BottomNavigationItem.SEARCH.res,
            onClick = searchClick
        )
        if (isOwnerOrEditor) {
            MenuItem(
                modifier = Modifier
                    .width(72.dp)
                    .height(52.dp),
                contentDescription = stringResource(id = R.string.main_navigation_content_desc_create_button),
                res = BottomNavigationItem.ADD_DOC.res,
                onClick = addDocClick,
                onLongClick = addDocLongClick
            )
        }
    }
}

@Composable
fun BottomNavigationMenu(
    state: NavPanelState,
    modifier: Modifier = Modifier,
    onShareButtonClicked: () -> Unit = {},
    onHomeButtonClicked: () -> Unit,
    searchClick: () -> Unit = {},
    addDocClick: () -> Unit = {},
    addDocLongClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .height(Height)
            .wrapContentWidth()
            .background(
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.home_screen_toolbar_button)
            )
            /**
             * Workaround for clicks through the bottom navigation menu.
             */
            .noRippleClickable { },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state is NavPanelState.Default) {
            when (state.leftButtonState) {
                is NavPanelState.LeftButtonState.AddMembers -> {
                    MenuItem(
                        modifier = Modifier
                            .width(72.dp)
                            .height(52.dp),
                        contentDescription = stringResource(id = R.string.main_navigation_content_desc_members_button),
                        res = BottomNavigationItem.ADD_MEMBERS.res,
                        onClick = onShareButtonClicked
                    )
                }

                is NavPanelState.LeftButtonState.Comment -> {
                    // TODO
                }
                NavPanelState.LeftButtonState.ViewMembers -> {
                    MenuItem(
                        modifier = Modifier
                            .width(72.dp)
                            .height(52.dp),
                        contentDescription = stringResource(id = R.string.main_navigation_content_desc_members_button),
                        res = BottomNavigationItem.MEMBERS.res,
                        onClick = onShareButtonClicked
                    )
                }
                is NavPanelState.LeftButtonState.Home -> {
                    MenuItem(
                        modifier = Modifier
                            .width(72.dp)
                            .height(52.dp),
                        contentDescription = stringResource(id = R.string.main_navigation_content_desc_home_button),
                        res = BottomNavigationItem.HOME.res,
                        onClick = onHomeButtonClicked
                    )
                }
            }
        } else {
            MenuItem(
                modifier = Modifier
                    .width(72.dp)
                    .height(52.dp),
                contentDescription = stringResource(id = R.string.main_navigation_content_desc_members_button),
                res = BottomNavigationItem.MEMBERS.res,
                onClick = onShareButtonClicked
            )
        }
        MenuItem(
            modifier = Modifier
                .width(72.dp)
                .height(52.dp)
                .alpha(
                    if (state is NavPanelState.Default) {
                        if (state.isCreateObjectButtonEnabled)
                            FULL_ALPHA
                        else
                            DEFAULT_DISABLED_ALPHA
                    } else {
                        DEFAULT_DISABLED_ALPHA
                    }
                )
            ,
            contentDescription = stringResource(id = R.string.main_navigation_content_desc_create_button),
            res = BottomNavigationItem.ADD_DOC.res,
            onClick = addDocClick,
            onLongClick = addDocLongClick,
            enabled = (state is NavPanelState.Default && state.isCreateObjectButtonEnabled)
        )
        MenuItem(
            modifier = Modifier
                .width(72.dp)
                .height(52.dp),
            contentDescription = stringResource(id = R.string.main_navigation_content_desc_search_button),
            res = BottomNavigationItem.SEARCH.res,
            onClick = searchClick
        )
    }
}

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
    MEMBERS(R.drawable.ic_nav_panel_members),
    ADD_MEMBERS(R.drawable.ic_nav_panel_add_member),
    SEARCH(R.drawable.ic_nav_panel_search),
    ADD_DOC(R.drawable.ic_nav_panel_plus)
}

@Immutable
object BottomNavigationDefaults {
    val Height = 52.dp
    val Width = 288.dp
}