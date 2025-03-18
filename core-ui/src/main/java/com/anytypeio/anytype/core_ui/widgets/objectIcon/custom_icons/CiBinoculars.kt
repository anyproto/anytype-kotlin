package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiBinoculars: ImageVector
    get() {
        if (_CiBinoculars != null) {
            return _CiBinoculars!!
        }
        _CiBinoculars = ImageVector.Builder(
            name = "CiBinoculars",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(114.92f, 82.83f)
                curveTo(126.82f, 70.76f, 142.71f, 64f, 164f, 64f)
                curveTo(182.44f, 64f, 197.93f, 71.24f, 208.61f, 83.61f)
                curveTo(219.06f, 95.71f, 224f, 111.64f, 224f, 128f)
                verticalLineTo(162.81f)
                curveTo(232.45f, 161.23f, 243.15f, 160f, 256f, 160f)
                curveTo(268.85f, 160f, 279.55f, 161.23f, 288f, 162.81f)
                verticalLineTo(128f)
                curveTo(288f, 111.64f, 292.94f, 95.71f, 303.39f, 83.61f)
                curveTo(314.07f, 71.24f, 329.56f, 64f, 348f, 64f)
                curveTo(369.29f, 64f, 385.18f, 70.76f, 397.08f, 82.83f)
                curveTo(408.15f, 94.06f, 414.63f, 108.87f, 419.88f, 122.11f)
                lineTo(420.51f, 123.78f)
                lineTo(488.72f, 305.69f)
                curveTo(488.72f, 305.69f, 491.31f, 313f, 491.63f, 314.08f)
                curveTo(494.47f, 323.56f, 496f, 333.6f, 496f, 344f)
                curveTo(496f, 401.44f, 449.44f, 448f, 392f, 448f)
                curveTo(334.56f, 448f, 288f, 401.44f, 288f, 344f)
                curveTo(288f, 343.96f, 288f, 343.92f, 288f, 343.89f)
                verticalLineTo(283.54f)
                curveTo(287.99f, 283.54f, 287.97f, 283.53f, 287.96f, 283.53f)
                curveTo(281.09f, 281.83f, 270.38f, 280f, 256f, 280f)
                curveTo(241.62f, 280f, 230.91f, 281.83f, 224.04f, 283.53f)
                lineTo(224f, 283.54f)
                verticalLineTo(344f)
                curveTo(224f, 401.44f, 177.44f, 448f, 120f, 448f)
                curveTo(62.56f, 448f, 16f, 401.44f, 16f, 344f)
                curveTo(16f, 333.6f, 17.53f, 323.56f, 20.37f, 314.08f)
                curveTo(20.52f, 313.39f, 20.72f, 312.7f, 20.97f, 312.02f)
                curveTo(21.55f, 310.28f, 22.61f, 307.39f, 23.28f, 305.69f)
                lineTo(92.13f, 122.11f)
                curveTo(97.37f, 108.87f, 103.85f, 94.06f, 114.92f, 82.83f)
                close()
                moveTo(320f, 343.9f)
                curveTo(320.05f, 304.18f, 352.27f, 272f, 392f, 272f)
                curveTo(422.37f, 272f, 448.35f, 290.81f, 458.93f, 317.41f)
                curveTo(459.39f, 318.67f, 459.85f, 319.9f, 460.29f, 321.12f)
                curveTo(462.7f, 328.31f, 464f, 336f, 464f, 344f)
                curveTo(464f, 383.76f, 431.76f, 416f, 392f, 416f)
                curveTo(352.25f, 416f, 320.02f, 383.79f, 320f, 344.04f)
                curveTo(320f, 344.03f, 320f, 344.01f, 320f, 344f)
                moveTo(120f, 272f)
                curveTo(89.63f, 272f, 63.65f, 290.8f, 53.07f, 317.41f)
                curveTo(52.61f, 318.67f, 52.16f, 319.91f, 51.71f, 321.12f)
                curveTo(49.3f, 328.31f, 48f, 336f, 48f, 344f)
                curveTo(48f, 383.76f, 80.24f, 416f, 120f, 416f)
                curveTo(159.76f, 416f, 192f, 383.76f, 192f, 344f)
                curveTo(192f, 304.24f, 159.76f, 272f, 120f, 272f)
                close()
            }
        }.build()

        return _CiBinoculars!!
    }

@Suppress("ObjectPropertyName")
private var _CiBinoculars: ImageVector? = null
