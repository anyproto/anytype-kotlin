package com.anytypeio.anytype.core_ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.getMimeIcon
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.widgets.objectIcon.BookmarkIconView
import com.anytypeio.anytype.core_ui.widgets.objectIcon.DeletedIconView
import com.anytypeio.anytype.core_ui.widgets.objectIcon.EmojiIconView
import com.anytypeio.anytype.core_ui.widgets.objectIcon.ImageIconView
import com.anytypeio.anytype.core_ui.widgets.objectIcon.ObjectIconProfile
import com.anytypeio.anytype.core_ui.widgets.objectIcon.TypeIconView
import com.anytypeio.anytype.presentation.objects.ObjectIcon

@Composable
fun ListWidgetObjectIcon(
    icon: ObjectIcon,
    modifier: Modifier,
    iconSize: Dp = 48.dp,
    onTaskIconClicked: (Boolean) -> Unit = {},
    backgroundColor: Int = R.color.shape_tertiary
) {
    when (icon) {
        is ObjectIcon.Profile -> {
            ObjectIconProfile(
                modifier = modifier,
                iconSize = iconSize,
                icon = icon
            )
        }

        is ObjectIcon.Basic.Emoji -> {
            EmojiIconView(
                icon = icon,
                backgroundSize = iconSize,
                modifier = modifier,
                backgroundColor = backgroundColor
            )
        }

        is ObjectIcon.Basic.Image -> {
            ImageIconView(
                icon = icon,
                backgroundSize = iconSize,
                modifier = modifier,
            )
        }

        is ObjectIcon.Bookmark -> {
            BookmarkIconView(
                modifier = modifier,
                icon = icon,
                backgroundSize = iconSize
            )
        }

        is ObjectIcon.Task -> {
            DefaultTaskObjectIcon(
                modifier = modifier,
                iconSize = iconSize,
                icon = icon,
                onIconClicked = onTaskIconClicked
            )
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
        ObjectIcon.Deleted -> {
            DeletedIconView(
                modifier = modifier,
                backgroundSize = iconSize
            )
        }

        is ObjectIcon.TypeIcon ->
            TypeIconView(
                icon = icon,
                backgroundSize = iconSize,
                modifier = modifier,
                backgroundColor = backgroundColor
            )

        is ObjectIcon.Checkbox -> {
            //do nothing
        }

        ObjectIcon.None -> {
            //do nothing
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