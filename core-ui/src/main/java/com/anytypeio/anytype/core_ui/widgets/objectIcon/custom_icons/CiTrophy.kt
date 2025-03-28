package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiTrophy: ImageVector
    get() {
        if (_CiTrophy != null) {
            return _CiTrophy!!
        }
        _CiTrophy = ImageVector.Builder(
            name = "CiTrophy",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(464f, 80f)
                lineTo(403.9f, 80f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4f, -4f)
                curveToRelative(0f, -4.89f, 0f, -9f, 0f, -12.08f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 367.9f, 32f)
                horizontalLineToRelative(0f)
                lineToRelative(-223.79f, 0.26f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, -31.94f, 31.93f)
                curveToRelative(0f, 3.23f, 0f, 7.22f, 0f, 11.81f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4f, 4f)
                lineTo(48f, 80f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 96f)
                verticalLineToRelative(16f)
                curveToRelative(0f, 54.53f, 30f, 112.45f, 76.52f, 125.35f)
                arcToRelative(7.82f, 7.82f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.55f, 5.9f)
                curveToRelative(5.77f, 26.89f, 23.52f, 52.5f, 51.41f, 73.61f)
                curveToRelative(20.91f, 15.83f, 45.85f, 27.5f, 68.27f, 32.48f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6.25f, 7.8f)
                lineTo(240f, 444f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4f, 4f)
                lineTo(176.45f, 448f)
                curveToRelative(-8.61f, 0f, -16f, 6.62f, -16.43f, 15.23f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 176f, 480f)
                lineTo(335.55f, 480f)
                curveToRelative(8.61f, 0f, 16f, -6.62f, 16.43f, -15.23f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 336f, 448f)
                lineTo(276f, 448f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -4f, -4f)
                lineTo(272f, 357.14f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6.25f, -7.8f)
                curveToRelative(22.42f, -5f, 47.36f, -16.65f, 68.27f, -32.48f)
                curveToRelative(27.89f, -21.11f, 45.64f, -46.72f, 51.41f, -73.61f)
                arcToRelative(7.82f, 7.82f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.55f, -5.9f)
                curveTo(450f, 224.45f, 480f, 166.53f, 480f, 112f)
                lineTo(480f, 96f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = false, 464f, 80f)
                close()
                moveTo(112f, 198.22f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -6f, 3.45f)
                curveToRelative(-10.26f, -6.11f, -17.75f, -15.37f, -22.14f, -21.89f)
                curveToRelative(-11.91f, -17.69f, -19f, -40.67f, -19.79f, -63.63f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, -4.15f)
                horizontalLineToRelative(40f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, 4f)
                curveTo(112.05f, 143.45f, 112f, 174.87f, 112f, 198.22f)
                close()
                moveTo(428.13f, 179.78f)
                curveToRelative(-4.39f, 6.52f, -11.87f, 15.78f, -22.13f, 21.89f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, -6f, -3.46f)
                curveToRelative(0f, -26.51f, 0f, -56.63f, -0.05f, -82.21f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, -4f)
                horizontalLineToRelative(40f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, 4.15f)
                curveTo(447.16f, 139.11f, 440.05f, 162.09f, 428.14f, 179.78f)
                close()
            }
        }.build()

        return _CiTrophy!!
    }

@Suppress("ObjectPropertyName")
private var _CiTrophy: ImageVector? = null
