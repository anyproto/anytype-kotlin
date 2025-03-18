package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiArrowUndoCircle: ImageVector
    get() {
        if (_CiArrowUndoCircle != null) {
            return _CiArrowUndoCircle!!
        }
        _CiArrowUndoCircle = ImageVector.Builder(
            name = "CiArrowUndoCircle",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 48f)
                curveTo(141.13f, 48f, 48f, 141.13f, 48f, 256f)
                reflectiveCurveToRelative(93.13f, 208f, 208f, 208f)
                reflectiveCurveToRelative(208f, -93.13f, 208f, -208f)
                reflectiveCurveTo(370.87f, 48f, 256f, 48f)
                close()
                moveTo(353.67f, 329.1f)
                curveToRelative(-24.07f, -25.21f, -51.51f, -38.68f, -108.58f, -38.68f)
                verticalLineToRelative(37.32f)
                arcToRelative(8.32f, 8.32f, 0f, isMoreThanHalf = false, isPositiveArc = true, -14.05f, 6f)
                lineTo(146.58f, 254f)
                arcToRelative(8.2f, 8.2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -11.94f)
                lineTo(231f, 162.29f)
                arcToRelative(8.32f, 8.32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14.05f, 6f)
                verticalLineToRelative(37.32f)
                curveToRelative(88.73f, 0f, 117.42f, 55.64f, 122.87f, 117.09f)
                curveTo(368.65f, 330.42f, 359.07f, 334.75f, 353.67f, 329.1f)
                close()
            }
        }.build()

        return _CiArrowUndoCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiArrowUndoCircle: ImageVector? = null
