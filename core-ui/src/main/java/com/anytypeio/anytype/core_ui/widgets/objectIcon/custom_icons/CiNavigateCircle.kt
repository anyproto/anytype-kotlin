package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiNavigateCircle: ImageVector
    get() {
        if (_CiNavigateCircle != null) {
            return _CiNavigateCircle!!
        }
        _CiNavigateCircle = ImageVector.Builder(
            name = "CiNavigateCircle",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(258.9f, 48f)
                curveTo(141.92f, 46.42f, 46.42f, 141.92f, 48f, 258.9f)
                curveTo(49.56f, 371.09f, 140.91f, 462.44f, 253.1f, 464f)
                curveToRelative(117f, 1.6f, 212.48f, -93.9f, 210.88f, -210.88f)
                curveTo(462.44f, 140.91f, 371.09f, 49.56f, 258.9f, 48f)
                close()
                moveTo(351f, 175.24f)
                lineTo(268.76f, 361.76f)
                curveToRelative(-4.79f, 10.47f, -20.78f, 7f, -20.78f, -4.56f)
                verticalLineTo(268f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4f, -4f)
                horizontalLineTo(154.8f)
                curveToRelative(-11.52f, 0f, -15f, -15.87f, -4.57f, -20.67f)
                lineTo(336.76f, 161f)
                arcTo(10.73f, 10.73f, 0f, isMoreThanHalf = false, isPositiveArc = true, 351f, 175.24f)
                close()
            }
        }.build()

        return _CiNavigateCircle!!
    }

@Suppress("ObjectPropertyName")
private var _CiNavigateCircle: ImageVector? = null
