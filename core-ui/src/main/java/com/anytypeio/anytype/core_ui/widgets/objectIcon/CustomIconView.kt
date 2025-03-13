package com.anytypeio.anytype.core_ui.widgets.objectIcon

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIcon
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor.Companion.fromIconOption
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconData
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconDataColor

@Composable
fun CustomIconView(
    customIconData: CustomIconData,
    modifier: Modifier,
    iconSize: Dp,
) {
    val context = LocalContext.current

    // Получаем имя иконки
    val iconName = customIconData.icon.stringRepresentation

    // Получаем идентификатор ресурса
    val resId = remember(iconName) {
        context.resources.getIdentifier("ci_$iconName", "drawable", context.packageName)
    }

    val tint = when (fromIconOption(customIconData.color.iconOption)) {
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
        null -> colorResource(id = R.color.glyph_active)
    }

    if (resId != 0) {
        Icon(
            modifier = modifier
                .size(iconSize),
            painter = painterResource(id = resId),
            tint = tint,
            contentDescription = "Object Type icon"
        )
    } else {
        // Если ресурс не найден, можно показать заглушку или текст
        Image(
            painter = painterResource(id = R.drawable.ic_bookmark_placeholder),
            contentDescription = "Object Type icon",
            contentScale = ContentScale.Crop,
            modifier = modifier.size(iconSize)
        )
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
            color = CustomIconDataColor.Selected(color = CustomIconColor.Yellow)
        ),
        modifier = Modifier,
        iconSize = 18.dp
    )
}