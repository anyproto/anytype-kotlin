package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiDice: ImageVector
    get() {
        if (_CiDice != null) {
            return _CiDice!!
        }
        _CiDice = ImageVector.Builder(
            name = "CiDice",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(440.88f, 129.37f)
                lineTo(288.16f, 40.62f)
                arcToRelative(64.14f, 64.14f, 0f, isMoreThanHalf = false, isPositiveArc = false, -64.33f, 0f)
                lineTo(71.12f, 129.37f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 6.9f)
                lineTo(254f, 243.85f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4.06f, 0f)
                lineTo(440.9f, 136.27f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 440.88f, 129.37f)
                close()
                moveTo(256f, 152f)
                curveToRelative(-13.25f, 0f, -24f, -7.16f, -24f, -16f)
                reflectiveCurveToRelative(10.75f, -16f, 24f, -16f)
                reflectiveCurveToRelative(24f, 7.16f, 24f, 16f)
                reflectiveCurveTo(269.25f, 152f, 256f, 152f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(238f, 270.81f)
                lineTo(54f, 163.48f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -6f, 3.46f)
                lineTo(48f, 340.86f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = false, 23.84f, 41.39f)
                lineTo(234f, 479.48f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6f, -3.46f)
                lineTo(240f, 274.27f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 238f, 270.81f)
                close()
                moveTo(96f, 368f)
                curveToRelative(-8.84f, 0f, -16f, -10.75f, -16f, -24f)
                reflectiveCurveToRelative(7.16f, -24f, 16f, -24f)
                reflectiveCurveToRelative(16f, 10.75f, 16f, 24f)
                reflectiveCurveTo(104.84f, 368f, 96f, 368f)
                close()
                moveTo(192f, 336f)
                curveToRelative(-8.84f, 0f, -16f, -10.75f, -16f, -24f)
                reflectiveCurveToRelative(7.16f, -24f, 16f, -24f)
                reflectiveCurveToRelative(16f, 10.75f, 16f, 24f)
                reflectiveCurveTo(200.84f, 336f, 192f, 336f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(458f, 163.51f)
                lineTo(274f, 271.56f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2f, 3.45f)
                lineTo(272f, 476f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6f, 3.46f)
                lineToRelative(162.15f, -97.23f)
                arcTo(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = false, 464f, 340.86f)
                lineTo(464f, 167f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 458f, 163.51f)
                close()
                moveTo(320f, 424f)
                curveToRelative(-8.84f, 0f, -16f, -10.75f, -16f, -24f)
                reflectiveCurveToRelative(7.16f, -24f, 16f, -24f)
                reflectiveCurveToRelative(16f, 10.75f, 16f, 24f)
                reflectiveCurveTo(328.84f, 424f, 320f, 424f)
                close()
                moveTo(320f, 336f)
                curveToRelative(-8.84f, 0f, -16f, -10.75f, -16f, -24f)
                reflectiveCurveToRelative(7.16f, -24f, 16f, -24f)
                reflectiveCurveToRelative(16f, 10.75f, 16f, 24f)
                reflectiveCurveTo(328.84f, 336f, 320f, 336f)
                close()
                moveTo(416f, 368f)
                curveToRelative(-8.84f, 0f, -16f, -10.75f, -16f, -24f)
                reflectiveCurveToRelative(7.16f, -24f, 16f, -24f)
                reflectiveCurveToRelative(16f, 10.75f, 16f, 24f)
                reflectiveCurveTo(424.84f, 368f, 416f, 368f)
                close()
                moveTo(416f, 280f)
                curveToRelative(-8.84f, 0f, -16f, -10.75f, -16f, -24f)
                reflectiveCurveToRelative(7.16f, -24f, 16f, -24f)
                reflectiveCurveToRelative(16f, 10.75f, 16f, 24f)
                reflectiveCurveTo(424.84f, 280f, 416f, 280f)
                close()
            }
        }.build()

        return _CiDice!!
    }

@Suppress("ObjectPropertyName")
private var _CiDice: ImageVector? = null
