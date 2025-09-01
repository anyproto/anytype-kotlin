package com.anytypeio.anytype.ui_settings.space.new_settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.getWallpaperGradientByCode
import com.anytypeio.anytype.core_ui.extensions.res
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.Caption2Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.presentation.wallpaper.WallpaperColor
import com.anytypeio.anytype.presentation.wallpaper.WallpaperView

@Composable
fun WallpaperSelectBottomSheet(
    state: List<WallpaperView>,
    onWallpaperSelected: (WallpaperView) -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.wallpaper),
                style = Title1,
                color = colorResource(id = R.color.text_primary)
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items = state) { item ->
                WallpaperItem(
                    modifier = Modifier
                        .noRippleThrottledClickable{
                            onWallpaperSelected(item)
                        },
                    wallpaper = item,
                    isSelected = item.isSelected
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun WallpaperItem(
    wallpaper: WallpaperView,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {

    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(208.dp)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) colorResource(id = R.color.control_accent_50) else Color.Transparent,
                shape = shape
            )
    ) {
        when (wallpaper) {
            is WallpaperView.Gradient -> {
                val gradient = getWallpaperGradientByCode(wallpaper.code)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = gradient?.toBrush() ?: Brush.horizontalGradient(
                                listOf(Color.Gray, Color.LightGray)
                            ),
                            shape = shape,
                            alpha = 0.3f
                        )
                )
            }

            is WallpaperView.SolidColor -> {
                val wallpaperColor = WallpaperColor.entries.find { it.code == wallpaper.code }
                val color = try {
                    Color(android.graphics.Color.parseColor(wallpaperColor?.hex ?: "#DFDDD1"))
                } catch (e: IllegalArgumentException) {
                    Color.LightGray
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = color.copy(alpha = 0.3f),
                            shape = shape,
                        )
                )
            }

            is WallpaperView.SpaceColor -> {
                val spaceIconColor = wallpaper.systemColor
                if (spaceIconColor != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = spaceIconColor.res().copy(alpha = 0.3f),
                                shape = shape,
                            )
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_blur_24),
                        contentDescription = "Space color icon"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.wallpaper_item_space_icon_color),
                        style = Caption2Regular,
                        color = colorResource(id = R.color.text_primary),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}