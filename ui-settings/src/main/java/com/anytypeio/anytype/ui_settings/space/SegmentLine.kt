package com.anytypeio.anytype.ui_settings.space

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.anytypeio.anytype.core_ui.views.NoRippleTheme
import com.anytypeio.anytype.presentation.settings.SpacesStorageViewModel
import com.anytypeio.anytype.ui_settings.R

@Composable
fun SegmentLine(items: List<SpacesStorageViewModel.SegmentLineItem>) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    Column(
        modifier = Modifier
            .height(27.dp)
            .fillMaxWidth()
            .onSizeChanged { size = it }
    ) {
        CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(27.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val freeWidth = with(LocalDensity.current) {
                    size.width.toDp() - (items.size - 1).dp * 2
                }
                val values = items.sumOf { it.value.toDouble() }
                val oneValueWidth = freeWidth / maxOf(values.toFloat(), 1f)

                items.forEach { item ->
                    val color = when (item) {
                        is SpacesStorageViewModel.SegmentLineItem.Active -> {
                            colorResource(id = R.color.palette_system_amber_125)
                        }
                        is SpacesStorageViewModel.SegmentLineItem.Free -> {
                            colorResource(id = R.color.shape_tertiary)
                        }
                        is SpacesStorageViewModel.SegmentLineItem.Other -> {
                            colorResource(id = R.color.palette_system_amber_50)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .width(maxOf(item.value.times(oneValueWidth), 4f.dp))
                            .height(27.dp)
                            .clip(MaterialTheme.shapes.medium.copy(CornerSize(5.dp)))
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
            }
        }
    }
}
