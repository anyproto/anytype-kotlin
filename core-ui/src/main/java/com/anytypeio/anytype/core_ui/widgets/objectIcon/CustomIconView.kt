package com.anytypeio.anytype.core_ui.widgets.objectIcon

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons.CustomIcons
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIcon
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor.Companion.fromIconOption
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconData

@Composable
fun CustomIconView(
    modifier: Modifier = Modifier,
    customIconData: CustomIconData,
    iconSize: Dp
) {
    val iconName = customIconData.icon.rawValue

    val tint = getCustomIconColorValue(customIconData.color)

    val imageVector = remember(iconName) {
        CustomIcons.getIconByName(iconName)
    }

    Box(modifier = modifier) {
        if (imageVector != null) {
            Image(
                modifier = Modifier.size(iconSize),
                imageVector = imageVector,
                contentDescription = "Object Type icon",
                colorFilter = ColorFilter.tint(tint),
            )
        }
    }
}

@Composable
fun getCustomIconColorValue(iconColor: CustomIconColor?): Color {
    return when (fromIconOption(iconColor?.iconOption)) {
        CustomIconColor.Gray -> colorResource(id = R.color.glyph_active)
        CustomIconColor.Yellow -> colorResource(id = R.color.palette_system_yellow)
        CustomIconColor.Amber -> colorResource(id = R.color.palette_system_amber_100)
        CustomIconColor.Red -> colorResource(id = R.color.palette_system_red)
        CustomIconColor.Pink -> colorResource(id = R.color.palette_system_pink)
        CustomIconColor.Purple -> colorResource(id = R.color.palette_system_purple)
        CustomIconColor.Blue -> colorResource(id = R.color.palette_system_blue)
        CustomIconColor.Sky -> colorResource(id = R.color.palette_system_sky)
        CustomIconColor.Teal -> colorResource(id = R.color.palette_system_teal)
        CustomIconColor.Green -> colorResource(id = R.color.palette_system_green)
        null -> colorResource(id = R.color.glyph_inactive)
    }
}

@Composable
@DefaultPreviews
fun CustomIconViewPreview() {
    CustomIconView(
        customIconData = CustomIconData(
            icon = CustomIcon(
                rawValue = "batteryCharging"
            ),
            color = CustomIconColor.Yellow
        ),
        modifier = Modifier,
        iconSize = 18.dp
    )
}