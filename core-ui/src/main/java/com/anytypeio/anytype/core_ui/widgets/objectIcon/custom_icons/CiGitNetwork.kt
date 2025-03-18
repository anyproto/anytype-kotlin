package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiGitNetwork: ImageVector
    get() {
        if (_CiGitNetwork != null) {
            return _CiGitNetwork!!
        }
        _CiGitNetwork = ImageVector.Builder(
            name = "CiGitNetwork",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(448f, 96f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = false, -96.31f, 55.21f)
                curveToRelative(-1.79f, 20.87f, -11.47f, 38.1f, -28.87f, 51.29f)
                curveTo(305.07f, 216f, 280.09f, 224f, 256f, 224f)
                reflectiveCurveToRelative(-49.07f, -8f, -66.82f, -21.5f)
                curveToRelative(-17.4f, -13.19f, -27.08f, -30.42f, -28.87f, -51.29f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = false, -64.11f, 0.29f)
                curveToRelative(2.08f, 40.87f, 21.17f, 76.87f, 54.31f, 102f)
                curveTo(171.3f, 269.26f, 197f, 280.19f, 224f, 285.09f)
                verticalLineToRelative(75.52f)
                arcToRelative(64f, 64f, 0f, isMoreThanHalf = true, isPositiveArc = false, 64f, 0f)
                verticalLineTo(285.09f)
                curveToRelative(27f, -4.9f, 52.7f, -15.83f, 73.49f, -31.59f)
                curveToRelative(33.14f, -25.13f, 52.23f, -61.13f, 54.31f, -102f)
                arcTo(64f, 64f, 0f, isMoreThanHalf = false, isPositiveArc = false, 448f, 96f)
                close()
                moveTo(128f, 64f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 96f, 96f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 128f, 64f)
                close()
                moveTo(256f, 448f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, -32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 256f, 448f)
                close()
                moveTo(384f, 128f)
                arcToRelative(32f, 32f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, -32f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = true, 384f, 128f)
                close()
            }
        }.build()

        return _CiGitNetwork!!
    }

@Suppress("ObjectPropertyName")
private var _CiGitNetwork: ImageVector? = null
