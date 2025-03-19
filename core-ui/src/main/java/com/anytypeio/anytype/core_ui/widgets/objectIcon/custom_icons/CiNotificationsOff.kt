package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiNotificationsOff: ImageVector
    get() {
        if (_CiNotificationsOff != null) {
            return _CiNotificationsOff!!
        }
        _CiNotificationsOff = ImageVector.Builder(
            name = "CiNotificationsOff",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(448f, 464f)
                arcToRelative(15.92f, 15.92f, 0f, isMoreThanHalf = false, isPositiveArc = true, -11.31f, -4.69f)
                lineToRelative(-384f, -384f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 75.31f, 52.69f)
                lineToRelative(384f, 384f)
                arcTo(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, 448f, 464f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(440.08f, 341.31f)
                curveToRelative(-1.66f, -2f, -3.29f, -4f, -4.89f, -5.93f)
                curveToRelative(-22f, -26.61f, -35.31f, -42.67f, -35.31f, -118f)
                curveToRelative(0f, -39f, -9.33f, -71f, -27.72f, -95f)
                curveToRelative(-13.56f, -17.73f, -31.89f, -31.18f, -56.05f, -41.12f)
                arcToRelative(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.82f, -0.67f)
                curveTo(306.6f, 51.49f, 282.82f, 32f, 256f, 32f)
                reflectiveCurveToRelative(-50.59f, 19.49f, -59.28f, 48.56f)
                arcToRelative(3.13f, 3.13f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.81f, 0.65f)
                arcToRelative(157.88f, 157.88f, 0f, isMoreThanHalf = false, isPositiveArc = false, -21.88f, 11f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.49f, 12.49f)
                lineTo(434.32f, 366.44f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 13.6f, -6.63f)
                arcTo(35.39f, 35.39f, 0f, isMoreThanHalf = false, isPositiveArc = false, 440.08f, 341.31f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(112.14f, 217.35f)
                curveToRelative(0f, 75.36f, -13.29f, 91.42f, -35.31f, 118f)
                curveToRelative(-1.6f, 1.93f, -3.23f, 3.89f, -4.89f, 5.93f)
                arcToRelative(35.16f, 35.16f, 0f, isMoreThanHalf = false, isPositiveArc = false, -4.65f, 37.62f)
                curveToRelative(6.17f, 13f, 19.32f, 21.07f, 34.33f, 21.07f)
                horizontalLineTo(312.8f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.66f, -13.66f)
                lineToRelative(-192f, -192f)
                arcToRelative(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -13.62f, 5f)
                quadTo(112.14f, 208f, 112.14f, 217.35f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(256f, 480f)
                arcToRelative(80.06f, 80.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, 70.44f, -42.13f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 322.9f, 432f)
                horizontalLineTo(189.12f)
                arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.55f, 5.87f)
                arcTo(80.06f, 80.06f, 0f, isMoreThanHalf = false, isPositiveArc = false, 256f, 480f)
                close()
            }
        }.build()

        return _CiNotificationsOff!!
    }

@Suppress("ObjectPropertyName")
private var _CiNotificationsOff: ImageVector? = null
