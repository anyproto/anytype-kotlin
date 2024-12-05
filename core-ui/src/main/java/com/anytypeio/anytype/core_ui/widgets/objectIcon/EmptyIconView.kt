package com.anytypeio.anytype.core_ui.widgets.objectIcon

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.widgets.cornerRadius
import com.anytypeio.anytype.core_ui.widgets.imageAsset
import com.anytypeio.anytype.presentation.objects.ObjectIcon

@Composable
fun EmptyIconView(
    modifier: Modifier = Modifier,
    backgroundSize: Dp,
    emptyType: ObjectIcon.Empty,
    iconWithoutBackgroundMaxSize: Dp = 20.dp,
    imageMultiplier: Float = 0.625f,
    backgroundColor: Int = R.color.shape_secondary
) {
    val (containerModifier, iconModifier) = if (backgroundSize > iconWithoutBackgroundMaxSize) {
        modifier
            .size(backgroundSize)
            .background(
                color = colorResource(backgroundColor),
                shape = RoundedCornerShape(size = cornerRadius(backgroundSize))
            ) to Modifier.size(
            width = backgroundSize * imageMultiplier,
            height = backgroundSize * imageMultiplier
        )
    } else {
        modifier.size(backgroundSize) to Modifier
    }

    Box(
        modifier = containerModifier,
        contentAlignment = Alignment.Center
    ) {

        val imageAsset = imageAsset(emptyType)

        Image(
            painter = painterResource(id = imageAsset),
            contentDescription = "Empty Object Icon",
            modifier = iconModifier
        )
    }
}

@DefaultPreviews
@Composable
fun Empty20ObjectIconViewPreview() {
    EmptyIconView(
        emptyType = ObjectIcon.Empty.Page,
        backgroundSize = 20.dp,
    )
}

@DefaultPreviews
@Composable
fun Empty32ObjectIconViewPreview() {
    EmptyIconView(
        emptyType = ObjectIcon.Empty.Page,
        backgroundSize = 32.dp,
    )
}

@DefaultPreviews
@Composable
fun Empty48ObjectIconViewPreview() {
    EmptyIconView(
        emptyType = ObjectIcon.Empty.Page,
        backgroundSize = 48.dp
    )
}

@DefaultPreviews
@Composable
fun Empty64ObjectIconViewPreview() {
    EmptyIconView(
        emptyType = ObjectIcon.Empty.Page,
        backgroundSize = 64.dp
    )
}

@DefaultPreviews
@Composable
fun Empty112ObjectIconViewPreview() {
    EmptyIconView(
        emptyType = ObjectIcon.Empty.Page,
        backgroundSize = 112.dp
    )
}