package com.anytypeio.anytype.ui_settings.appearance

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.SpaceEvenly
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.ThemeMode
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Option
import com.anytypeio.anytype.core_ui.foundation.Toolbar
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption2Regular
import com.anytypeio.anytype.ui_settings.R


private val buttonSize = 60.dp
private const val firstQuarterFactor = 0.5f
private const val thirdQuartersFactor = 1.5f

@Composable
fun AppearanceScreen(
    onWallpaperClicked: () -> Unit,
    light: () -> Unit,
    dark: () -> Unit,
    system: () -> Unit,
    selectedMode: ThemeMode
) {
    Column {
        Box(
            Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Dragger()
        }
        Toolbar(stringResource(R.string.appearance))
        Option(
            image = R.drawable.ic_wallpaper,
            text = stringResource(R.string.wallpaper),
            onClick = onWallpaperClicked
        )
        Divider(paddingStart = 60.dp)
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 12.dp),
            textAlign = TextAlign.Center,
            text = stringResource(R.string.mode),
            style = Caption1Medium,
            color = colorResource(R.color.text_secondary),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = SpaceEvenly
        ) {

            LightModeButton(
                onClick = light,
                selectedMode == ThemeMode.Light
            )
            DarkModeButton(
                onClick = dark,
                selectedMode == ThemeMode.Night
            )
            SystemModeButton(
                onClick = system,
                selectedMode == ThemeMode.System
            )
        }
        Divider(paddingStart = 20.dp)
        Box(Modifier.height(48.dp))
    }
}

@Composable
fun LightModeButton(
    onClick: () -> Unit = {},
    isSelected: Boolean
) {
    ButtonColumn(onClick = onClick) {
        SelectionBox(isSelected = isSelected) {
            Text(
                text = "Aa",
                style = MaterialTheme.typography.h1,
                modifier = Modifier
                    .size(buttonSize)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = colorResource(id = R.color.shape_primary),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .wrapContentSize(align = Alignment.Center)
            )
        }
        ModeNameText(id = R.string.light)
    }
}

@Composable
fun DarkModeButton(
    onClick: () -> Unit = {},
    isSelected: Boolean
) {
    ButtonColumn(onClick = onClick) {
        SelectionBox(isSelected = isSelected) {
            Text(
                text = "Aa",
                style = MaterialTheme.typography.h1,
                color = colorResource(id = R.color.text_white),
                modifier = Modifier
                    .size(buttonSize)
                    .background(
                        color = Color.Black,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .wrapContentSize(align = Alignment.Center)
            )
        }
        ModeNameText(id = R.string.dark)
    }
}

@Composable
fun SystemModeButton(
    onClick: () -> Unit = {},
    isSelected: Boolean
) {
    ButtonColumn(onClick = onClick) {
        SelectionBox(isSelected = isSelected) {

            val cornersRadius = with(LocalDensity.current) { 14.dp.toPx() }
            val greyCornerRadius = with(LocalDensity.current) { 15.dp.toPx() }
            val greyBorderSize = with(LocalDensity.current) { 1.dp.toPx() }
            val greyColor = colorResource(id = R.color.shape_primary)

            Canvas(
                modifier = Modifier.size(buttonSize)
            ) {

                val rect = Rect(Offset.Zero, size)

                drawWholeViewGreyRoundedRectangle(this, rect, greyColor, greyCornerRadius)
                drawFirstQuarterWhiteRoundedRectangle(this, rect, cornersRadius, greyBorderSize)
                drawSecondQuarterWhiteRectangle(this, rect, greyBorderSize)

                drawThirdQuarterBlackRectangle(this, rect)
                drawFourthQuarterBlackRoundedRectangle(this, rect, cornersRadius)

            }
            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(Color.Black)) {
                    append("A")
                }
                withStyle(style = SpanStyle(Color.White)) {
                    append("a")
                }
            }
            Text(
                text = annotatedString,
                style = MaterialTheme.typography.h1,
                modifier = Modifier
                    .size(buttonSize)
                    .wrapContentSize(align = Alignment.Center)
            )
        }
        ModeNameText(id = R.string.system)
    }
}

@Composable
fun SelectionBox(
    isSelected: Boolean,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .let {
                if (isSelected) it.border(
                    width = 2.dp,
                    color = colorResource(id = R.color.amber25),
                    shape = RoundedCornerShape(18.dp)
                ) else {
                    it
                }
            }
            .padding(4.dp),
        content = content
    )
}

@Composable
fun ButtonColumn(
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .wrapContentWidth(Alignment.CenterHorizontally)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
fun ModeNameText(
    @StringRes id: Int
) {
    Text(
        text = stringResource(id = id),
        style = Caption2Regular,
        color = colorResource(R.color.text_secondary),
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Preview()
@Composable
fun ComposablePreview() {
    AppearanceScreen({}, {}, {}, {}, ThemeMode.Light)
}

fun drawThirdQuarterBlackRectangle(
    drawScope: DrawScope,
    rect: Rect
) {
    with(drawScope) {
        val path = Path().apply {
            moveTo(rect.topCenter.x, rect.topCenter.y)
            lineTo(rect.topCenter.x * thirdQuartersFactor, rect.topCenter.y)
            lineTo(rect.bottomCenter.x * thirdQuartersFactor, rect.bottomCenter.y)
            lineTo(rect.bottomCenter.x, rect.bottomCenter.y)
            close()
        }

        drawPathIntoCanvas(this, path, Color.Black)
    }
}

fun drawFourthQuarterBlackRoundedRectangle(
    drawScope: DrawScope,
    rect: Rect,
    cornersRadius: Float
) {
    with(drawScope) {
        val path = Path().apply {
            moveTo(rect.topCenter.x * thirdQuartersFactor - cornersRadius, rect.topCenter.y)
            lineTo(rect.topRight)
            lineTo(rect.bottomRight)
            lineTo(rect.bottomCenter.x * thirdQuartersFactor - cornersRadius, rect.bottomCenter.y)
            close()
        }
        drawPathIntoCanvas(this, path, Color.Black, PathEffect.cornerPathEffect(cornersRadius))
    }
}

fun drawSecondQuarterWhiteRectangle(
    drawScope: DrawScope,
    rect: Rect,
    greyBorderSize: Float
) {
    with(drawScope) {
        val path = Path().apply {
            moveTo(rect.topCenter.x, rect.topCenter.y + greyBorderSize)
            lineTo(rect.topCenter.x * firstQuarterFactor, rect.topCenter.y + greyBorderSize)
            lineTo(rect.bottomCenter.x * firstQuarterFactor, rect.bottomCenter.y - greyBorderSize)
            lineTo(rect.bottomCenter.x, rect.bottomCenter.y - greyBorderSize)
            close()
        }
        drawPathIntoCanvas(this, path, Color.White, null)
    }
}

fun drawFirstQuarterWhiteRoundedRectangle(
    drawScope: DrawScope,
    rect: Rect,
    cornersRadius: Float,
    greyBorderSize: Float
) {
    with(drawScope) {
        val path = Path().apply {
            moveTo(
                rect.topCenter.x * firstQuarterFactor + cornersRadius,
                rect.topCenter.y + greyBorderSize
            )
            lineTo(rect.topLeft.x + greyBorderSize, rect.topLeft.y + greyBorderSize)
            lineTo(rect.bottomLeft.x + greyBorderSize, rect.bottomLeft.y - greyBorderSize)
            lineTo(
                rect.bottomCenter.x * firstQuarterFactor + cornersRadius,
                rect.bottomCenter.y - greyBorderSize
            )
            close()
        }

        drawPathIntoCanvas(this, path, Color.White, PathEffect.cornerPathEffect(cornersRadius))
    }
}

fun drawWholeViewGreyRoundedRectangle(
    drawScope: DrawScope,
    rect: Rect,
    greyColor: Color,
    greyCornerRadius: Float
) {
    with(drawScope) {
        val path = Path().apply {
            moveTo(rect.topLeft.x, rect.topLeft.y)
            lineTo(rect.topRight.x, rect.topRight.y)
            lineTo(rect.bottomRight.x, rect.bottomRight.y)
            lineTo(rect.bottomLeft.x, rect.bottomLeft.y)
            close()
        }

        drawPathIntoCanvas(this, path, greyColor, PathEffect.cornerPathEffect(greyCornerRadius))
    }
}

fun drawPathIntoCanvas(
    drawScope: DrawScope,
    path: Path,
    toColor: Color,
    toPathEffect: PathEffect? = null
) {
    with(drawScope) {
        drawIntoCanvas { canvas ->
            canvas.drawOutline(
                outline = Outline.Generic(path),
                paint = Paint().apply {
                    color = toColor
                    pathEffect = toPathEffect
                }
            )
        }
    }
}

private fun Path.lineTo(offset: Offset) = lineTo(offset.x, offset.y)