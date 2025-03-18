package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCube: ImageVector
    get() {
        if (_CiCube != null) {
            return _CiCube!!
        }
        _CiCube = ImageVector.Builder(
            name = "CiCube",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(440.9f, 136.3f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -6.91f)
                lineTo(288.16f, 40.65f)
                arcToRelative(64.14f, 64.14f, 0f, isMoreThanHalf = false, isPositiveArc = false, -64.33f, 0f)
                lineTo(71.12f, 129.39f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 6.91f)
                lineTo(254f, 243.88f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4.06f, 0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(54f, 163.51f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 48f, 167f)
                verticalLineTo(340.89f)
                arcToRelative(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = false, 23.84f, 41.39f)
                lineTo(234f, 479.51f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6f, -3.46f)
                verticalLineTo(274.3f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2f, -3.46f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(272f, 275f)
                verticalLineToRelative(201f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6f, 3.46f)
                lineToRelative(162.15f, -97.23f)
                arcTo(48f, 48f, 0f, isMoreThanHalf = false, isPositiveArc = false, 464f, 340.89f)
                verticalLineTo(167f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -6f, -3.45f)
                lineToRelative(-184f, 108f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 272f, 275f)
                close()
            }
        }.build()

        return _CiCube!!
    }

@Suppress("ObjectPropertyName")
private var _CiCube: ImageVector? = null
