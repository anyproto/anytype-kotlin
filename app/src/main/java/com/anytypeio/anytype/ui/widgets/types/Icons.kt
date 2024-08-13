package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.objects.ObjectIcon

@Composable
fun TreeWidgetObjectIcon(
    modifier: Modifier = Modifier,
    icon: ObjectIcon,
    paddingStart: Dp,
    paddingEnd: Dp,
    onTaskIconClicked: (Boolean) -> Unit,
    size: Dp = 18.dp
) {
    when (icon) {
        is ObjectIcon.Profile.Avatar -> {
            Box(
                modifier = modifier
                    .padding(start = paddingStart, end = paddingEnd)
                    .size(size)
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
                        .uppercase()
                    ,
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
            UriCircleImage(
                uri = icon.hash,
                modifier = modifier.padding(start = paddingStart, end = paddingEnd),
                size = size
            )
        }
        is ObjectIcon.Basic.Emoji -> {
            val emoji = Emojifier.safeUri(icon.unicode)
            if (emoji != Emojifier.Config.EMPTY_URI) {
                UriImage(
                    uri = Emojifier.safeUri(icon.unicode),
                    modifier = modifier.padding(start = paddingStart, end = paddingEnd),
                    size = size
                )
            } else {
                Text(
                    text = icon.unicode,
                    modifier = modifier.padding(start = paddingStart, end = paddingEnd),
                    fontSize = 16.sp,
                    maxLines = 1
                )
            }
        }
        is ObjectIcon.Basic.Image -> {
            UriImage(
                uri = icon.hash,
                modifier = modifier.padding(start = paddingStart, end = paddingEnd),
                size = size
            )
        }
        is ObjectIcon.Bookmark -> {
            UriImage(
                uri = icon.image,
                modifier = modifier.padding(start = paddingStart, end = paddingEnd),
                size = size
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
                    .size(size)
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
    modifier: Modifier,
    size: Dp = 18.dp
) {
    Image(
        painter = rememberAsyncImagePainter(uri),
        contentDescription = "Icon from URI",
        modifier = modifier.size(size),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun UriCircleImage(
    uri: String,
    modifier: Modifier,
    size: Dp = 18.dp
) {
    Image(
        painter = rememberAsyncImagePainter(uri),
        contentDescription = "Icon from URI",
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}