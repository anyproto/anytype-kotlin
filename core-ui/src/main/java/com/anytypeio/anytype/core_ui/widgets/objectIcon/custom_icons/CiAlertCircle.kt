package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiAlertCircle: ImageVector
    get() {
        if (_CiAlertCircle != null) {
            return _CiAlertCircle!!
        }
        _CiAlertCircle = ImageVector.Builder(
            name = "CiAlertCircle",
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
                moveTo(256f, 367.91f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = true, isPositiveArc = true, 20f, -20f)
                arcTo(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 367.91f)
                close()
                moveTo(277.72f, 166.76f)
                lineTo(271.98f, 288.76f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -32f, 0f)
                lineToRelative(-5.74f, -121.94f)
                verticalLineToRelative(-0.05f)
                arcToRelative(21.74f, 21.74f, 0f, isMoreThanHalf = true, isPositiveArc = true, 43.44f, 0f)
                close()
            }
        }.build()

        return _CiAlertCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiAlertCircle: ImageVector? = null
