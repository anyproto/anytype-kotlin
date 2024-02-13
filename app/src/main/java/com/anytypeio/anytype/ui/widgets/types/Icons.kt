package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            UriImage(
                uri = icon.hash,
                modifier = modifier.padding(start = paddingStart, end = paddingEnd)
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