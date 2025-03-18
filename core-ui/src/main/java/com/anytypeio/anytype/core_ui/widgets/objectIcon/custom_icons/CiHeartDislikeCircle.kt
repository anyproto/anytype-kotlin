package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiHeartDislikeCircle: ImageVector
    get() {
        if (_CiHeartDislikeCircle != null) {
            return _CiHeartDislikeCircle!!
        }
        _CiHeartDislikeCircle = ImageVector.Builder(
            name = "CiHeartDislikeCircle",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 48f)
                curveTo(141.31f, 48f, 48f, 141.31f, 48f, 256f)
                reflectiveCurveToRelative(93.31f, 208f, 208f, 208f)
                reflectiveCurveToRelative(208f, -93.31f, 208f, -208f)
                reflectiveCurveTo(370.69f, 48f, 256f, 48f)
                close()
                moveTo(279.3f, 347.19f)
                curveToRelative(-4.41f, 3.2f, -9.16f, 6.55f, -14.31f, 10f)
                arcToRelative(15.93f, 15.93f, 0f, isMoreThanHalf = false, isPositiveArc = true, -18f, 0f)
                curveToRelative(-39.3f, -26.68f, -56.32f, -45f, -65.7f, -56.41f)
                curveToRelative(-20f, -24.37f, -29.58f, -49.4f, -29.3f, -76.5f)
                curveToRelative(0f, -0.21f, 0f, -0.43f, 0f, -0.64f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6.82f, -2.72f)
                lineTo(279.76f, 341.12f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 279.3f, 347.19f)
                close()
                moveTo(347.3f, 363.31f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.62f, 0f)
                lineToRelative(-176f, -176f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.62f, -22.62f)
                lineToRelative(176f, 176f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 347.31f, 363.31f)
                close()
                moveTo(333.2f, 297.69f)
                arcToRelative(3.92f, 3.92f, 0f, isMoreThanHalf = false, isPositiveArc = true, -6f, 0.37f)
                lineToRelative(-124f, -123.21f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 206f, 168f)
                lineToRelative(1.55f, 0f)
                curveToRelative(20.4f, 0f, 35f, 10.64f, 44.11f, 20.42f)
                arcToRelative(5.93f, 5.93f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8.7f, 0f)
                curveToRelative(9.11f, -9.78f, 23.71f, -20.42f, 44.11f, -20.42f)
                curveToRelative(30.31f, 0f, 55.22f, 25.27f, 55.53f, 56.33f)
                curveTo(360.26f, 250.26f, 351.48f, 274.3f, 333.2f, 297.69f)
                close()
            }
        }.build()

        return _CiHeartDislikeCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiHeartDislikeCircle: ImageVector? = null
