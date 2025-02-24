package com.anytypeio.anytype.ui_settings.space

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.spaces.UiSpaceSettingsItem
import com.anytypeio.anytype.ui_settings.R

@Composable
fun MembersItem(
    modifier: Modifier = Modifier,
    item: UiSpaceSettingsItem.Members
) {
    BaseButton(
        modifier = modifier,
        title = stringResource(id = R.string.space_settings_members_button_members),
        icon = R.drawable.ic_members_24,
        count = item.count
    )
}

@Composable
fun ObjectTypesItem(
    modifier: Modifier = Modifier
) {
    BaseButton(
        modifier = modifier,
        title = stringResource(id = R.string.space_settings_types_button),
        icon = R.drawable.ic_object_types_24,
    )
}

@Composable
fun DefaultTypeItem(
    modifier: Modifier = Modifier,
    name: String,
    icon: ObjectIcon
) {
    Row(
        modifier = modifier
            .border(
                shape = RoundedCornerShape(16.dp),
                width = 0.5.dp,
                color = colorResource(id = R.color.shape_primary)
            )
            .padding(vertical = 20.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.space_settings_default_type_button),
            style = PreviewTitle1Regular,
            color = colorResource(id = R.color.text_primary),
        )
        ListWidgetObjectIcon(
            modifier = Modifier,
            iconSize = 20.dp,
            icon = icon
        )
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = name.take(10),
            style = PreviewTitle1Regular,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = colorResource(id = R.color.text_primary),
        )
        Image(
            painter = painterResource(id = R.drawable.ic_disclosure_8_24),
            contentDescription = "Members icon",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun WallpaperItem(
    modifier: Modifier = Modifier,
    item: UiSpaceSettingsItem.Wallpapers
) {
    Row(
        modifier = modifier
            .border(
                shape = RoundedCornerShape(16.dp),
                width = 0.5.dp,
                color = colorResource(id = R.color.shape_primary)
            )
            .padding(vertical = 20.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.space_settings_wallpaper_button),
            style = PreviewTitle1Regular,
            color = colorResource(id = R.color.text_primary),
        )
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    color = light(item.color),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 6.dp),
        )
        Image(
            painter = painterResource(id = R.drawable.ic_disclosure_8_24),
            contentDescription = "Members icon",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SpaceInfoItem(
    modifier: Modifier = Modifier
) {
    BaseButton(
        modifier = modifier,
        title = stringResource(id = R.string.space_settings_space_info_button),
    )
}

@Composable
fun DeleteSpaceItem(
    modifier: Modifier = Modifier
) {
    BaseButton(
        modifier = modifier,
        title = stringResource(id = R.string.space_settings_delete_space_button),
        textColor = R.color.palette_dark_red
    )
}

@Composable
fun BaseButton(
    modifier: Modifier = Modifier,
    icon: Int? = null,
    title: String,
    count: Int? = null,
    textColor: Int = R.color.text_primary,
) {
    Row(
        modifier = modifier
            .border(
                shape = RoundedCornerShape(16.dp),
                width = 0.5.dp,
                color = colorResource(id = R.color.shape_primary)
            )
            .padding(vertical = 20.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        if (icon != null) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = "Members icon",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = PreviewTitle1Regular,
            color = colorResource(id = textColor),
        )
        if (count != null) {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .background(
                        color = colorResource(id = R.color.transparent_active),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                    .padding(horizontal = 6.dp),
                text = "$count",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = Caption1Regular,
                color = colorResource(id = R.color.text_white),
            )
        }
        Image(
            painter = painterResource(id = R.drawable.ic_disclosure_8_24),
            contentDescription = "Members icon",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
@DefaultPreviews
fun MembersItemPreview() {
    Column {
        MembersItem(
            item = UiSpaceSettingsItem.Members(
                count = 5
            )
        )
    }

}