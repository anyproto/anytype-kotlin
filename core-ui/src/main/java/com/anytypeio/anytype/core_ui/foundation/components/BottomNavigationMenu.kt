package com.anytypeio.anytype.core_ui.foundation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.anytypeio.anytype.core_ui.foundation.components.BottomNavigationDefaults.Height
import com.anytypeio.anytype.core_ui.foundation.components.BottomNavigationDefaults.Width
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleCombinedClickable
import com.anytypeio.anytype.presentation.profile.ProfileIconView


@Composable
fun BottomNavigationMenu(
    modifier: Modifier = Modifier,
    backClick: () -> Unit = {},
    backLongClick: () -> Unit = {},
    searchClick: () -> Unit = {},
    addDocClick: () -> Unit = {},
    onCreateObjectLongClicked: () -> Unit = {},
    isOwnerOrEditor: Boolean
) {
    Row(
        modifier = modifier
            .height(Height)
            .width(Width)
            .background(
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.home_screen_toolbar_button)
            )
            /**
             * Workaround for clicks through the bottom navigation menu.
             */
            .noRippleClickable { },
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MenuItem(
            res = BottomNavigationItem.BACK.res,
            onClick = backClick,
            onLongClick = backLongClick
        )
        MenuItem(
            res = BottomNavigationItem.ADD_DOC.res,
            onClick = addDocClick,
            onLongClick = onCreateObjectLongClicked,
            enabled = isOwnerOrEditor
        )
        MenuItem(
            res = BottomNavigationItem.SEARCH.res,
            onClick = searchClick
        )
    }
}

@Composable
private fun MenuItem(
    @DrawableRes res: Int,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    Image(
        painter = painterResource(id = res),
        contentDescription = "",
        alpha = if (enabled) 1f else 0.5f,
        modifier = Modifier.noRippleCombinedClickable(
            onClick = {
                if (enabled) {
                    onClick()
                }
            },
            onLongClicked = {
                if (enabled) {
                    haptic.performHapticFeedback(
                        HapticFeedbackType.LongPress
                    )
                    onLongClick()
                }
            }
        )
    )
}

@Composable
private fun ProfileMenuItem(
    icon: ProfileIconView,
    onClick: () -> Unit = {}
) {
    when (icon) {
        is ProfileIconView.Image -> {
            Image(
                painter = rememberAsyncImagePainter(
                    model = icon.url,
                    error = painterResource(id = R.drawable.ic_home_widget_space)
                ),
                contentDescription = "Custom image profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .noRippleClickable { onClick() }
            )
        }

        is ProfileIconView.Placeholder -> {
            val name = icon.name
            val nameFirstChar = if (name.isNullOrEmpty()) {
                stringResource(id = R.string.account_default_name)
            } else {
                name.first().uppercaseChar().toString()
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colorResource(id = R.color.shape_primary))
                    .noRippleClickable { onClick() }
            ) {
                Text(
                    text = nameFirstChar,
                    style = MaterialTheme.typography.h3.copy(
                        color = colorResource(id = R.color.text_white),
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        else -> {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.shape_primary))
                    .noRippleClickable { onClick() }
            )
        }
    }
}


private enum class BottomNavigationItem(@DrawableRes val res: Int) {
    BACK(R.drawable.ic_nav_panel_back),
    HOME(R.drawable.ic_nav_panel_vault),
    SEARCH(R.drawable.ic_nav_panel_search),
    ADD_DOC(R.drawable.ic_nav_panel_plus)
}

@Immutable
object BottomNavigationDefaults {
    val Height = 52.dp
    val Width = 288.dp
}