package com.anytypeio.anytype.core_ui.widgets.objectIcon

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.colorRes
import com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons.CustomIcons
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIcon
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor

@Composable
fun CustomIconView(
    modifier: Modifier = Modifier,
    icon: ObjectIcon.ObjectType,
    iconSize: Dp
) {
    val tint = colorResource(id = icon.color.colorRes())

    val imageVector = CustomIcons.getImageVector(icon.icon)

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
@DefaultPreviews
fun CustomIconViewPreview() {
    CustomIconView(
        icon = ObjectIcon.ObjectType(
            icon = CustomIcon(
                rawValue = "batteryCharging"
            ),
            color = CustomIconColor.Yellow
        ),
        modifier = Modifier,
        iconSize = 18.dp
    )
}