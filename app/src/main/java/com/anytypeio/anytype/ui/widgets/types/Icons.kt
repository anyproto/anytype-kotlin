package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.ui_settings.main.GradientComposeView

@Composable
fun TreeWidgetObjectIcon(
    modifier: Modifier = Modifier,
    icon: ObjectIcon,
    paddingStart: Dp,
    paddingEnd: Dp,
    onTaskIconClicked: (Boolean) -> Unit
) {
    when (icon) {
        is ObjectIcon.Profile.Avatar -> {
            Box(
                modifier = modifier
                    .padding(start = paddingStart, end = paddingEnd)
                    .height(18.dp)
                    .width(18.dp)
                    .background(
                        shape = CircleShape,
                        color = colorResource(id = R.color.shape_primary)
                    )
            ) {
                Text(
                    text = icon.name.ifEmpty { stringResource(id = R.string.u) }.take(1),
                    modifier = Modifier.align(Alignment.Center),
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(id = R.color.text_white)
                    )
                )
            }
        }
        is ObjectIcon.Profile.Image -> {
            UriImage(
                uri = icon.hash,
                modifier = modifier.padding(start = paddingStart, end = paddingEnd)
            )
        }
        is ObjectIcon.Profile.Gradient -> {
            GradientComposeView(
                modifier = modifier.padding(start = paddingStart, end = paddingEnd),
                from = icon.from,
                to = icon.to,
                size = 18.dp
            )
        }
        is ObjectIcon.Basic.Emoji -> {
            UriImage(
                uri = Emojifier.safeUri(icon.unicode),
                modifier = modifier.padding(start = paddingStart, end = paddingEnd)
            )
        }
        is ObjectIcon.Basic.Image -> {
            UriImage(
                uri = icon.hash,
                modifier = Modifier.padding(start = paddingStart, end = paddingEnd)
            )
        }
        is ObjectIcon.Bookmark -> {
            UriImage(
                uri = icon.image,
                modifier = Modifier.padding(start = paddingStart, end = paddingEnd)
            )
        }
        is ObjectIcon.Task -> {
            Image(
                painter = if (icon.isChecked)
                    painterResource(id = R.drawable.ic_dashboard_task_checkbox_checked)
                else
                    painterResource(id = R.drawable.ic_dashboard_task_checkbox_not_checked),
                contentDescription = "Task icon",
                modifier = modifier
                    .padding(start = paddingStart, end = paddingEnd)
                    .noRippleClickable { onTaskIconClicked(icon.isChecked) }
            )
        }
        else -> {
            // Draw nothing.
        }
    }
}

@Composable
fun UriImage(
    uri: String,
    modifier: Modifier
) {
    Image(
        painter = rememberAsyncImagePainter(uri),
        contentDescription = "Icon from URI",
        modifier = modifier
            .height(18.dp)
            .width(18.dp)
    )
}

@Composable
fun ListWidgetObjectIcon(
    icon: ObjectIcon,
    modifier: Modifier,
    iconSize: Dp = 48.dp,
    onTaskIconClicked: (Boolean) -> Unit = {}
) {
    when (icon) {
        is ObjectIcon.Profile.Avatar -> DefaultProfileAvatarIcon(modifier, iconSize, icon)
        is ObjectIcon.Profile.Image -> defaultProfileIconImage(icon, modifier, iconSize)
        is ObjectIcon.Profile.Gradient -> GradientComposeView(
            modifier = modifier,
            from = icon.from,
            to = icon.to,
            size = iconSize
        )
        is ObjectIcon.Basic.Emoji -> DefaultEmojiObjectIcon(modifier, iconSize, icon)
        is ObjectIcon.Basic.Image -> DefaultObjectImageIcon(icon.hash, modifier, iconSize)
        is ObjectIcon.Bookmark -> DefaultObjectBookmarkIcon(icon.image, modifier, iconSize)
        is ObjectIcon.Task -> DefaultTaskObjectIcon(modifier, iconSize, icon, onTaskIconClicked)
        else -> {
            // Draw nothing.
        }
    }
}

@Composable
private fun DefaultTaskObjectIcon(
    modifier: Modifier,
    iconSize: Dp,
    icon: ObjectIcon.Task,
    onIconClicked: (Boolean) -> Unit
) {
    Box(
        modifier = modifier
            .size(iconSize)
            .noRippleClickable { onIconClicked(icon.isChecked) }
    ) {
        Image(
            painter = if (icon.isChecked)
                painterResource(id = R.drawable.ic_gallery_view_task_checked)
            else
                painterResource(id = R.drawable.ic_dashboard_task_checkbox_not_checked),
            contentDescription = "Task icon",
            modifier = Modifier
                .align(Alignment.Center)
                .size(24.dp)
        )
    }
}

@Composable
private fun DefaultObjectImageIcon(
    url: Url,
    modifier: Modifier,
    iconSize: Dp
) {
    Image(
        painter = rememberAsyncImagePainter(model = url),
        contentDescription = "Icon from URI",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(iconSize)
            .clip(RoundedCornerShape(2.dp))
    )
}

@Composable
private fun DefaultObjectBookmarkIcon(
    url: Url,
    modifier: Modifier,
    iconSize: Dp
) {
    Box(modifier = modifier.size(iconSize)) {
        Image(
            painter = rememberAsyncImagePainter(url),
            contentDescription = "Icon from URI",
            modifier = Modifier
                .align(Alignment.Center)
                .size(24.dp)
        )
    }
}

@Composable
private fun DefaultProfileAvatarIcon(
    modifier: Modifier,
    iconSize: Dp,
    icon: ObjectIcon.Profile.Avatar
) {
    Box(
        modifier = modifier
            .size(iconSize)
            .background(
                shape = CircleShape,
                color = colorResource(id = R.color.shape_primary)
            )
    ) {
        val name = icon.name.ifEmpty { stringResource(id = R.string.u) }
        Text(
            text = name.take(1),
            modifier = Modifier.align(Alignment.Center),
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.text_white)
            )
        )
    }
}

@Composable
private fun defaultProfileIconImage(
    icon: ObjectIcon.Profile.Image,
    modifier: Modifier,
    iconSize: Dp
) {
    Image(
        painter = rememberAsyncImagePainter(icon.hash),
        contentDescription = "Icon from URI",
        modifier = modifier
            .size(iconSize)
            .clip(CircleShape)
    )
}

@Composable
private fun DefaultEmojiObjectIcon(
    modifier: Modifier,
    iconSize: Dp,
    icon: ObjectIcon.Basic.Emoji
) {
    Box(
        modifier = modifier
            .size(iconSize)
            .background(
                shape = RoundedCornerShape(12.dp),
                color = colorResource(id = R.color.shape_transparent)
            )
    ) {
        Image(
            painter = rememberAsyncImagePainter(Emojifier.safeUri(icon.unicode)),
            contentDescription = "Icon from URI",
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.Center)
        )
    }
}