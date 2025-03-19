package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiScanCircle: ImageVector
    get() {
        if (_CiScanCircle != null) {
            return _CiScanCircle!!
        }
        _CiScanCircle = ImageVector.Builder(
            name = "CiScanCircle",
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
                moveTo(216f, 368f)
                lineTo(188f, 368f)
                arcToRelative(44.05f, 44.05f, 0f, isMoreThanHalf = false, isPositiveArc = true, -44f, -44f)
                lineTo(144f, 296f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 0f)
                verticalLineToRelative(28f)
                arcToRelative(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 12f, 12f)
                horizontalLineToRelative(28f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 32f)
                close()
                moveTo(216f, 176f)
                lineTo(188f, 176f)
                arcToRelative(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = false, -12f, 12f)
                verticalLineToRelative(28f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -32f, 0f)
                lineTo(144f, 188f)
                arcToRelative(44.05f, 44.05f, 0f, isMoreThanHalf = false, isPositiveArc = true, 44f, -44f)
                horizontalLineToRelative(28f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 32f)
                close()
                moveTo(368f, 324f)
                arcToRelative(44.05f, 44.05f, 0f, isMoreThanHalf = false, isPositiveArc = true, -44f, 44f)
                lineTo(296f, 368f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -32f)
                horizontalLineToRelative(28f)
                arcToRelative(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = false, 12f, -12f)
                lineTo(336f, 296f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 32f, 0f)
                close()
                moveTo(368f, 216f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -32f, 0f)
                lineTo(336f, 188f)
                arcToRelative(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = false, -12f, -12f)
                lineTo(296f, 176f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -32f)
                horizontalLineToRelative(28f)
                arcToRelative(44.05f, 44.05f, 0f, isMoreThanHalf = false, isPositiveArc = true, 44f, 44f)
                close()
            }
        }.build()

        return _CiScanCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiScanCircle: ImageVector? = null
