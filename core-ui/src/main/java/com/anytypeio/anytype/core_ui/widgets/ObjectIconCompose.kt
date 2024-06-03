package com.anytypeio.anytype.core_ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.getMimeIcon
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.objects.ObjectIcon

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
        is ObjectIcon.Basic.Emoji -> DefaultEmojiObjectIcon(modifier, iconSize, icon)
        is ObjectIcon.Basic.Image -> DefaultObjectImageIcon(icon.hash, modifier, iconSize)
        is ObjectIcon.Bookmark -> DefaultObjectBookmarkIcon(icon.image, modifier, iconSize)
        is ObjectIcon.Task -> DefaultTaskObjectIcon(modifier, iconSize, icon, onTaskIconClicked)
        is ObjectIcon.File -> {
            DefaultFileObjectImageIcon(
                fileName = icon.fileName.orEmpty(),
                mime = icon.mime.orEmpty(),
                modifier = modifier,
                iconSize = iconSize
            )
        }
        else -> {
            // Draw nothing.
        }
    }
}

@Composable
fun DefaultTaskObjectIcon(
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
fun DefaultObjectImageIcon(
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
fun DefaultObjectBookmarkIcon(
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
fun DefaultProfileAvatarIcon(
    modifier: Modifier,
    iconSize: Dp,
    icon: ObjectIcon.Profile.Avatar
) {
    Box(
        modifier = modifier
            .size(iconSize)
            .background(
                shape = CircleShape,
                color = colorResource(id = R.color.text_tertiary)
            )
    ) {
        Text(
            text = icon
                .name
                .ifEmpty { stringResource(id = R.string.u) }
                .take(1)
                .uppercase(),
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
fun DefaultBasicAvatarIcon(
    modifier: Modifier,
    iconSize: Dp,
    icon: ObjectIcon.Basic.Avatar
) {
    Box(
        modifier = modifier
            .size(iconSize)
            .background(
                shape = RoundedCornerShape(12.dp),
                color = colorResource(id = R.color.text_tertiary)
            )
    ) {
        Text(
            text = icon
                .name
                .ifEmpty { stringResource(id = R.string.u) }
                .take(1)
                .uppercase(),
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
fun defaultProfileIconImage(
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
fun DefaultEmojiObjectIcon(
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

@Composable
fun DefaultFileObjectImageIcon(
    fileName: String,
    mime: String,
    modifier: Modifier,
    iconSize: Dp
) {
    val mimeIcon = mime.getMimeIcon(fileName)
    Image(
        painter = painterResource(id = mimeIcon),
        contentDescription = "File icon",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(iconSize)
            .clip(RoundedCornerShape(2.dp))
    )
}