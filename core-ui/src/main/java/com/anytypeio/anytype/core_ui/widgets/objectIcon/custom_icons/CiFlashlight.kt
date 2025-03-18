package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiFlashlight: ImageVector
    get() {
        if (_CiFlashlight != null) {
            return _CiFlashlight!!
        }
        _CiFlashlight = ImageVector.Builder(
            name = "CiFlashlight",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(462f, 216f)
                curveToRelative(9.35f, -9.35f, 15.14f, -19.09f, 17.19f, -28.95f)
                curveToRelative(2.7f, -12.95f, -1.29f, -25.55f, -11.22f, -35.48f)
                lineTo(360.43f, 44.05f)
                curveTo(346.29f, 29.92f, 322f, 24.07f, 296f, 50f)
                lineToRelative(-2f, 2f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 11.32f)
                lineTo(448.64f, 218f)
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 460f, 218f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(250.14f, 153.08f)
                lineToRelative(-0.16f, 2.34f)
                curveToRelative(-0.53f, 7.18f, -6.88f, 19.15f, -13.88f, 26.14f)
                lineTo(47.27f, 370.36f)
                curveToRelative(-11.12f, 11.11f, -16.46f, 25.57f, -15.05f, 40.7f)
                curveTo(33.49f, 424.58f, 40.16f, 438f, 51f, 448.83f)
                lineTo(63.17f, 461f)
                curveToRelative(12.61f, 12.6f, 27.78f, 19f, 42.49f, 19f)
                arcToRelative(50.4f, 50.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 36f, -15.24f)
                lineToRelative(188.84f, -188.8f)
                curveToRelative(7.07f, -7.07f, 18.84f, -13.3f, 26.17f, -13.87f)
                curveToRelative(17.48f, -1.32f, 43.57f, -3.28f, 67.79f, -15.65f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1f, -6.37f)
                lineTo(271.69f, 86.31f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -6.39f, 1f)
                curveTo(253.18f, 110.3f, 251.48f, 134.22f, 250.14f, 153.08f)
                close()
                moveTo(240.19f, 299.91f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -25.25f)
                arcTo(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = true, 240.19f, 299.91f)
                close()
            }
        }.build()

        return _CiFlashlight!!
    }

@Suppress("ObjectPropertyName")
private var _CiFlashlight: ImageVector? = null
