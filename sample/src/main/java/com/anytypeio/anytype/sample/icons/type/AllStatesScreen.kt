package com.anytypeio.anytype.sample.icons.type

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.ui.CustomIconColor
import com.anytypeio.anytype.core_ui.common.bottomBorder
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.views.animations.LoadingIndicator
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.sample.R

@Composable
fun AllStatesScreen() {
    val basicModifier = Modifier
        .height(72.dp)
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .bottomBorder()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "Type Icons, All state comparison",
                style = Title1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    ObjectIcon.Basic.Image(
                        hash = "https://sample-videos.com/img/Sample-jpg-image-5mb.jpg"
                    ),
                    modifier = Modifier
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.Basic.Image], Large Image 5 MB, iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    ObjectIcon.Basic.Image(
                        hash = "http://slowwly.robertomurray.co.uk/delay/9000/url/https://sample-videos.com/img/Sample-jpg-image-5mb.jpg"
                    ),
                    modifier = Modifier
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.Basic.Image], Loading state, iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    ObjectIcon.Basic.Image(
                        hash = "https://sample1111-videos.com/img/Sample-jpg-image-5mb.jpg",
                        fallback = ObjectIcon.TypeIcon.Fallback(
                            rawValue = "document"
                        )
                    ),
                    modifier = Modifier
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.Basic.Image], fallback on type icon: iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    ObjectIcon.Basic.Image(
                        hash = "https://sample1111-videos.com/img/Sample-jpg-image-5mb.jpg",
                        fallback = ObjectIcon.TypeIcon.Fallback(
                            rawValue = "document111"
                        )
                    ),
                    modifier = Modifier
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.Basic.Image], fallback on broken type icon: iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    ObjectIcon.Basic.Image(
                        hash = "https://sample1111-videos.com/img/Sample-jpg-image-5mb.jpg"
                    ),
                    modifier = Modifier
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.Basic.Image], fallback is null: iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    ObjectIcon.Basic.Emoji("😀",),
                    modifier = Modifier
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.Basic.Emoji], iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    ObjectIcon.Basic.Emoji(
                        "😀1",
                        fallback = ObjectIcon.TypeIcon.Fallback(
                            rawValue = "document"
                        )
                    ),
                    modifier = Modifier
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.Basic.Emoji], fallback on type icon: iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    ObjectIcon.Basic.Emoji(
                        "😀1",
                        fallback = ObjectIcon.TypeIcon.Fallback(
                            rawValue = "documentWRONG"
                        )
                    ),
                    modifier = Modifier
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.Basic.Emoji], broken emoji, fallback on broken type icon: iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    icon = ObjectIcon.Basic.Emoji("😀1"),
                    modifier = Modifier
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.Basic.Emoji], broken emoji, fallback is null: iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    icon = ObjectIcon.Profile.Avatar(name = "S"),
                    modifier = Modifier
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "Profile.Avatar, iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    icon = ObjectIcon.Profile.Image(
                        hash = "https://images.pexels.com/photos/7356468/pexels-photo-7356468.jpeg",
                        name = "F"
                    ),
                    modifier = Modifier
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "Profile.Image, iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    icon = ObjectIcon.Profile.Image(
                        hash = "httpsWRONG://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSUtI0xCQrGAC2ka6TlHrjYHcStjZitJwz-fA&s",
                        name = "X"
                    ),
                    modifier = Modifier
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "Profile.Image, fallback to name, iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LoadingIndicator(
                    containerModifier = Modifier,
                    containerSize = 48.dp,
                    withCircleBackground = true
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "Profile.Loading, iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    icon = ObjectIcon.TypeIcon.Default(
                        rawValue = "file-tray-full",
                        color = CustomIconColor.Teal
                    ),
                    modifier = Modifier,
                    iconSize = 48.dp
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.TypeIcon.Default] show colored type icon, iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    icon = ObjectIcon.TypeIcon.Default(
                        rawValue = "file-tray-full-wrong",
                        color = CustomIconColor.Teal
                    ),
                    modifier = Modifier,
                    iconSize = 48.dp
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.Type.Default] type icon is wrong, show Grey fallback, iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    icon = ObjectIcon.TypeIcon.Emoji(
                        unicode = "🤔",
                        rawValue = "file-tray-full",
                        color = CustomIconColor.Amber
                    ),
                    modifier = Modifier,
                    iconSize = 48.dp
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.Type.Emoji] show emoji in case of Pre-Primitives, iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    icon = ObjectIcon.TypeIcon.Emoji(
                        unicode = "🤔1",
                        rawValue = "file-tray-full",
                        color = CustomIconColor.Amber
                    ),
                    modifier = Modifier,
                    iconSize = 48.dp
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.Type.Emoji] wrong unicode -> fallback, iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    icon = ObjectIcon.TypeIcon.Emoji(
                        unicode = "🤔1",
                        rawValue = "file-tray-full-BROKEN",
                        color = CustomIconColor.Amber
                    ),
                    modifier = Modifier,
                    iconSize = 48.dp
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.Type.Emoji], wrong unicode, broken fallback, iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    icon = ObjectIcon.TypeIcon.Fallback(rawValue = "document"),
                    modifier = Modifier,
                    iconSize = 48.dp
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.TypeIcon.Fallback], for objects without image or emoji, iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    icon = ObjectIcon.TypeIcon.Fallback(rawValue = "document-WRONG"),
                    modifier = Modifier,
                    iconSize = 48.dp
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.TypeIcon.Fallback], wrong state, iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    icon = ObjectIcon.TypeIcon.Deleted,
                    modifier = Modifier,
                    iconSize = 48.dp
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.TypeIcon.Deleted], iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    ObjectIcon.Task(
                        isChecked = true
                    ),
                    modifier = Modifier
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.Task], checked, iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    ObjectIcon.Task(
                        isChecked = false
                    ),
                    modifier = Modifier
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.Task], unchecked, iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    ObjectIcon.Bookmark(
                        image = "https://sample-videos.com/img/Sample-jpg-image-5mb.jpg",
                        fallback = ObjectIcon.TypeIcon.Fallback(
                            rawValue = "bookmark"
                        )
                    ),
                    modifier = Modifier
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.Bookmark], iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    ObjectIcon.Bookmark(
                        image = "https://sampleWrong-videos.com/img/Sample-jpg-image-5mb.jpg",
                        fallback = ObjectIcon.TypeIcon.Fallback(
                            rawValue = "bookmark"
                        )
                    ),
                    modifier = Modifier
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.Bookmark], fallback, iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        item {
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    ObjectIcon.Deleted,
                    modifier = Modifier
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "[ObjectIcon.Deleted], iconSize 48",
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}