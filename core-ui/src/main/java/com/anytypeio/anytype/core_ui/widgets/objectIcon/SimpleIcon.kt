package com.anytypeio.anytype.core_ui.widgets.objectIcon

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons.CiExtensionPuzzle
import com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons.CustomIcons
import com.anytypeio.anytype.presentation.objects.ObjectIcon

@Composable
fun SimpleIcon(
    modifier: Modifier = Modifier,
    icon: ObjectIcon.SimpleIcon,
    backgroundSize: Dp,
) {
    val (imageVector, tint) = CustomIcons.getImageVector(icon.rawValue) to colorResource(id = icon.color)
    IconBoxView(
        boxModifier = modifier.size(backgroundSize),
        imageModifier = Modifier.size(backgroundSize),
        imageVector = imageVector ?: CustomIcons.CiExtensionPuzzle,
        contentDescription = "Simple icon",
        tint = tint
    )
}