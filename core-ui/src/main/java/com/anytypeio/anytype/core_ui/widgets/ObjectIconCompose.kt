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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.getMimeIcon
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.widgets.objectIcon.AvatarIconView
import com.anytypeio.anytype.core_ui.widgets.objectIcon.DeletedIconView
import com.anytypeio.anytype.core_ui.widgets.objectIcon.EmojiIconView
import com.anytypeio.anytype.core_ui.widgets.objectIcon.EmptyIconView
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
        is ObjectIcon.Profile.Avatar -> {
            AvatarIconView(
                modifier = modifier,
                iconSize = iconSize,
                icon = icon
            )
        }
        is ObjectIcon.Profile.Image -> {
            DefaultProfileIconImage(icon, modifier, iconSize)
        }
        is ObjectIcon.Basic.Emoji -> {
            EmojiIconView(icon = icon, backgroundSize = iconSize, modifier = modifier)
        }
        is ObjectIcon.Basic.Image -> {
            DefaultObjectImageIcon(icon.hash, modifier, iconSize)
        }
        is ObjectIcon.Bookmark -> {
            DefaultObjectBookmarkIcon(icon.image, modifier, iconSize)
        }
        is ObjectIcon.Task -> {
            DefaultTaskObjectIcon(modifier, iconSize, icon, onTaskIconClicked)
        }
        is ObjectIcon.File -> {
            DefaultFileObjectImageIcon(
                fileName = icon.fileName.orEmpty(),
                mime = icon.mime.orEmpty(),
                modifier = modifier,
                iconSize = iconSize,
                extension = icon.extensions
            )
        }
        is ObjectIcon.Checkbox -> {}
        ObjectIcon.Deleted -> {
            DeletedIconView(
                modifier = modifier,
                backgroundSize = iconSize
            )
        }
        is ObjectIcon.Empty -> {
            EmptyIconView(
                modifier = modifier,
                emptyType = icon,
                backgroundSize = iconSize
            )
        }
        ObjectIcon.None -> {}
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
fun DefaultProfileIconImage(
    icon: ObjectIcon.Profile.Image,
    modifier: Modifier,
    iconSize: Dp
) {
    Image(
        painter = rememberAsyncImagePainter(icon.hash),
        contentDescription = "Icon from URI",
        modifier = modifier
            .size(iconSize)
            .clip(CircleShape),
        contentScale = ContentScale.Crop,
    )
}

@Deprecated("Use DefaultEmojiIconView instead")
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
        val emoji = Emojifier.safeUri(icon.unicode)
        if (emoji != Emojifier.Config.EMPTY_URI) {
            Image(
                painter = rememberAsyncImagePainter(Emojifier.safeUri(icon.unicode)),
                contentDescription = "Icon from URI",
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center)
            )
        } else {
            Text(
                text = icon.unicode,
                modifier = Modifier.align(Alignment.Center),
                fontSize = 22.sp,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun DefaultFileObjectImageIcon(
    fileName: String,
    mime: String,
    modifier: Modifier,
    iconSize: Dp,
    extension: String?,
) {
    val mimeIcon = mime.getMimeIcon(extension)
    Image(
        painter = painterResource(id = mimeIcon),
        contentDescription = "File icon",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(iconSize)
            .clip(RoundedCornerShape(2.dp))
    )
}

fun cornerRadius(size: Dp): Dp {
    return when (size) {
        in 0.dp..20.dp -> 2.dp
        in 21.dp..39.dp -> 4.dp
        in 40.dp..47.dp -> 5.dp
        in 48.dp..63.dp -> 6.dp
        in 64.dp..79.dp -> 8.dp
        else -> 12.dp
    }
}

fun imageAsset(emptyType: ObjectIcon.Empty): Int {
    return when (emptyType) {
        ObjectIcon.Empty.Bookmark -> R.drawable.ic_empty_state_link
        ObjectIcon.Empty.Discussion -> R.drawable.ic_empty_state_chat
        ObjectIcon.Empty.List -> R.drawable.ic_empty_state_list
        ObjectIcon.Empty.ObjectType -> R.drawable.ic_empty_state_type
        ObjectIcon.Empty.Page -> R.drawable.ic_empty_state_page
    }
}