package com.anytypeio.anytype.ui_settings.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.ui_settings.R

@Composable
fun SpaceHeader(
    name: String?,
    icon: SpaceIconView?,
    modifier: Modifier = Modifier,
    onNameSet: (String) -> Unit,
    onRandomGradientClicked: () -> Unit
) {
    val isSpaceIconMenuExpanded = remember {
        mutableStateOf(false)
    }
    Box(modifier = modifier.padding(vertical = 6.dp)) {
        Dragger()
    }
    Box(modifier = modifier.padding(top = 12.dp, bottom = 28.dp)) {
        SpaceNameBlock()
    }
    Box(modifier = modifier.padding(bottom = 16.dp)) {
        if (icon != null) {
            SpaceImageBlock(
                icon = icon,
                onSpaceIconClick = {
                    isSpaceIconMenuExpanded.value = !isSpaceIconMenuExpanded.value
                },
                gradientCornerRadius = 4.dp
            )
            DropdownMenu(
                expanded = isSpaceIconMenuExpanded.value,
                offset = DpOffset(x = 0.dp, y = 6.dp),
                onDismissRequest = {
                    isSpaceIconMenuExpanded.value = false
                }
            ) {
                DropdownMenuItem(
                    onClick = {
                        onRandomGradientClicked()
                        isSpaceIconMenuExpanded.value = false
                    },
                ) {
                    Text(
                        text = stringResource(R.string.space_settings_apply_random_gradient),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary)
                    )
                }
            }
        }
    }
    if (name != null) {
        SpaceNameBlock(
            modifier = Modifier,
            name = name,
            onNameSet = onNameSet
        )
    }
}

@Composable
fun GradientComposeView(
    modifier: Modifier,
    from: String,
    to: String,
    size: Dp
) {
    val gradient = Brush.radialGradient(
        colors = listOf(
            Color(from.toColorInt()),
            Color(to.toColorInt())
        )
    )
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(gradient)
    )
}