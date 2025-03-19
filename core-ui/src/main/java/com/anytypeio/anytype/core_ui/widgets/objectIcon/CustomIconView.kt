package com.anytypeio.anytype.core_ui.widgets.objectIcon

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.colorRes
import com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons.CustomIcons
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIcon
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.widgets.cornerRadius

@Composable
fun CustomIconView(
    modifier: Modifier = Modifier,
    icon: ObjectIcon.ObjectType,
    backgroundSize: Dp,
    iconWithoutBackgroundMaxSize: Dp = 20.dp,
    imageMultiplier: Float = 0.625f,
) {
    val tint = colorResource(id = icon.icon.color.colorRes())

    val imageVector = CustomIcons.getImageVector(icon.icon)

    val (containerModifier, iconModifier) = if (backgroundSize > iconWithoutBackgroundMaxSize) {
        modifier.size(backgroundSize) to Modifier.size(
            width = backgroundSize * imageMultiplier,
            height = backgroundSize * imageMultiplier
        )
    } else {
        modifier.size(backgroundSize) to Modifier
    }

    Box(
        modifier = containerModifier,
        contentAlignment = Alignment.Center,
    ) {
        if (imageVector != null) {
            Image(
                modifier = iconModifier,
                imageVector = imageVector,
                contentDescription = "Object Type icon",
                colorFilter = ColorFilter.tint(tint),
            )
        } else {
            Image(
                modifier = iconModifier,
                painter = painterResource(id = R.drawable.ic_empty_state_page),
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
                rawValue = "batteryCharging",
                color = CustomIconColor.Yellow
            ),
        ),
        modifier = Modifier,
        backgroundSize = 18.dp
    )
}