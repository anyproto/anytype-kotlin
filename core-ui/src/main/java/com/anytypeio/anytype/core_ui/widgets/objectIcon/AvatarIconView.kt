package com.anytypeio.anytype.core_ui.widgets.objectIcon

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.AvatarTitle
import com.anytypeio.anytype.core_ui.views.animations.LoadingIndicator


val avatarBackgroundColor = R.color.shape_tertiary
val avatarTextColor = R.color.glyph_active

@Composable
fun ObjectIconProfile(
    modifier: Modifier,
    iconSize: Dp,
    icon: ObjectIcon.Profile,
    isCircleShape: Boolean = true
) {
    when (icon) {
        is ObjectIcon.Profile.Avatar -> {
            ProfileAvatarView(
                modifier = modifier,
                iconSize = iconSize,
                icon = icon,
                isCircleShape = isCircleShape
            )
        }

        is ObjectIcon.Profile.Image -> {
            ProfileImageView(
                modifier = modifier,
                iconSize = iconSize,
                icon = icon
            )
        }
    }

}

@Composable
private fun ProfileAvatarView(
    modifier: Modifier,
    iconSize: Dp,
    icon: ObjectIcon.Profile.Avatar,
    isCircleShape: Boolean = true
) {
    val (radius, fontSize) = getAvatarIconParams(iconSize)

    Box(
        modifier = modifier
            .size(iconSize)
            .background(
                shape = if (isCircleShape) {
                    CircleShape
                } else {
                    RoundedCornerShape(radius)
                },
                color = colorResource(id = avatarBackgroundColor)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon
                .name
                .ifEmpty { stringResource(id = R.string.u) }
                .take(1)
                .uppercase(),
            style = AvatarTitle,
            fontSize = fontSize.sp,
            color = colorResource(id = avatarTextColor)
        )
    }
}

@Composable
fun ProfileImageView(
    icon: ObjectIcon.Profile.Image,
    modifier: Modifier,
    iconSize: Dp
) {
    val painter = rememberAsyncImagePainter(icon.hash)
    val state by painter.state.collectAsState()

    when (state) {
        AsyncImagePainter.State.Empty,
        is AsyncImagePainter.State.Loading -> {
            LoadingIndicator(
                containerModifier = modifier,
                containerSize = iconSize,
                withCircleBackground = true
            )
        }

        is AsyncImagePainter.State.Error -> {
            ProfileAvatarView(
                modifier = modifier,
                iconSize = iconSize,
                icon = ObjectIcon.Profile.Avatar(name = icon.name)
            )
        }

        is AsyncImagePainter.State.Success -> {
            Image(
                painter = painter,
                contentDescription = "Icon from URI",
                modifier = modifier
                    .size(iconSize)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

private fun getAvatarIconParams(size: Dp): Pair<Int, Int> {
    return when (size) {
        in 0.dp..17.dp -> 2 to 10
        in 18.dp..19.dp -> 2 to 11
        in 20.dp..21.dp -> 2 to 13
        in 22.dp..25.dp -> 3 to 14
        in 26.dp..29.dp -> 3 to 16
        in 30.dp..31.dp -> 4 to 20
        in 32.dp..39.dp -> 4 to 20
        in 40.dp..47.dp -> 5 to 24
        in 48.dp..63.dp -> 6 to 28
        in 64.dp..95.dp -> 8 to 40
        in 96.dp..127.dp -> 12 to 64
        else -> 12 to 72
    }
}