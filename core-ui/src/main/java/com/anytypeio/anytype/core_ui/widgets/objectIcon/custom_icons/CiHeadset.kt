package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiHeadset: ImageVector
    get() {
        if (_CiHeadset != null) {
            return _CiHeadset!!
        }
        _CiHeadset = ImageVector.Builder(
            name = "CiHeadset",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(411.16f, 97.46f)
                curveTo(368.43f, 55.86f, 311.88f, 32f, 256f, 32f)
                reflectiveCurveTo(143.57f, 55.86f, 100.84f, 97.46f)
                curveTo(56.45f, 140.67f, 32f, 197f, 32f, 256f)
                curveToRelative(0f, 26.67f, 8.75f, 61.09f, 32.88f, 125.55f)
                reflectiveCurveTo(137f, 473f, 157.27f, 477.41f)
                curveToRelative(5.81f, 1.27f, 12.62f, 2.59f, 18.73f, 2.59f)
                arcToRelative(60.06f, 60.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, 30f, -8f)
                lineToRelative(14f, -8f)
                curveToRelative(15.07f, -8.82f, 19.47f, -28.13f, 10.8f, -43.35f)
                lineTo(143.88f, 268.08f)
                arcToRelative(31.73f, 31.73f, 0f, isMoreThanHalf = false, isPositiveArc = false, -43.57f, -11.76f)
                lineToRelative(-13.69f, 8f)
                arcToRelative(56.49f, 56.49f, 0f, isMoreThanHalf = false, isPositiveArc = false, -14f, 11.59f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -7f, -2f)
                arcTo(114.68f, 114.68f, 0f, isMoreThanHalf = false, isPositiveArc = true, 64f, 256f)
                curveToRelative(0f, -50.31f, 21f, -98.48f, 59.16f, -135.61f)
                curveTo(160f, 84.55f, 208.39f, 64f, 256f, 64f)
                reflectiveCurveToRelative(96f, 20.55f, 132.84f, 56.39f)
                curveTo(427f, 157.52f, 448f, 205.69f, 448f, 256f)
                arcToRelative(114.68f, 114.68f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.68f, 17.91f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -7f, 2f)
                arcToRelative(56.49f, 56.49f, 0f, isMoreThanHalf = false, isPositiveArc = false, -14f, -11.59f)
                lineToRelative(-13.69f, -8f)
                arcToRelative(31.73f, 31.73f, 0f, isMoreThanHalf = false, isPositiveArc = false, -43.57f, 11.76f)
                lineTo(281.2f, 420.65f)
                curveToRelative(-8.67f, 15.22f, -4.27f, 34.53f, 10.8f, 43.35f)
                lineToRelative(14f, 8f)
                arcToRelative(60.06f, 60.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, 30f, 8f)
                curveToRelative(6.11f, 0f, 12.92f, -1.32f, 18.73f, -2.59f)
                curveTo(375f, 473f, 423f, 446f, 447.12f, 381.55f)
                reflectiveCurveTo(480f, 282.67f, 480f, 256f)
                curveTo(480f, 197f, 455.55f, 140.67f, 411.16f, 97.46f)
                close()
            }
        }.build()

        return _CiHeadset!!
    }

@Suppress("ObjectPropertyName")
private var _CiHeadset: ImageVector? = null
