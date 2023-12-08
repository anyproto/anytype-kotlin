package com.anytypeio.anytype.core_ui.foundation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleCombinedClickable
import com.anytypeio.anytype.presentation.profile.ProfileIconView


@Composable
fun BottomNavigationMenu(
    modifier: Modifier = Modifier,
    backClick: () -> Unit = {},
    homeClick: () -> Unit = {},
    searchClick: () -> Unit = {},
    addDocClick: () -> Unit = {},
    onCreateObjectLongClicked: () -> Unit = {},
    onProfileClicked: () -> Unit = {},
    profileIcon: ProfileIconView = ProfileIconView.Loading
) {
    Row(
        modifier = modifier
            .height(Height)
            .fillMaxWidth()
            .background(color = colorResource(id = R.color.background_primary))
            /**
             * Workaround for clicks through the bottom navigation menu.
             */
            .noRippleClickable { },
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MenuItem(BottomNavigationItem.BACK.res, onClick = backClick)
        MenuItem(BottomNavigationItem.HOME.res, onClick = homeClick)
        MenuItem(
            BottomNavigationItem.ADD_DOC.res,
            onClick = addDocClick,
            onLongClick = onCreateObjectLongClicked
        )
        MenuItem(BottomNavigationItem.SEARCH.res, onClick = searchClick)
        ProfileMenuItem(
            icon = profileIcon,
            onClick = onProfileClicked
        )
    }
}

@Composable
private fun MenuItem(
    @DrawableRes res: Int,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    Image(
        painter = painterResource(id = res),
        contentDescription = "",
        modifier = Modifier.noRippleCombinedClickable(
            onClick = onClick,
            onLongClicked = {
                haptic.performHapticFeedback(
                    HapticFeedbackType.LongPress
                )
                onLongClick()
            }
        )
    )
}

@Composable
private fun ProfileMenuItem(
    icon: ProfileIconView,
    onClick: () -> Unit = {}
) {
    when(icon) {
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
    BACK(R.drawable.ic_main_toolbar_back),
    HOME(R.drawable.ic_main_toolbar_home),
    SEARCH(R.drawable.ic_page_toolbar_search),
    ADD_DOC(R.drawable.ic_page_toolbar_add_doc)
}

@Immutable
private object BottomNavigationDefaults {
    val Height = 48.dp
}