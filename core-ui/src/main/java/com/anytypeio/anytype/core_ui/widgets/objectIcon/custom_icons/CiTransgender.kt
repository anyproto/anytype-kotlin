package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiTransgender: ImageVector
    get() {
        if (_CiTransgender != null) {
            return _CiTransgender!!
        }
        _CiTransgender = ImageVector.Builder(
            name = "CiTransgender",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(458f, 32f)
                horizontalLineTo(390f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 44f)
                horizontalLineToRelative(14.89f)
                lineToRelative(-59.57f, 59.57f)
                arcToRelative(149.69f, 149.69f, 0f, isMoreThanHalf = false, isPositiveArc = false, -178.64f, 0f)
                lineTo(159.11f, 128f)
                lineToRelative(26.45f, -26.44f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, -31.12f, -31.12f)
                lineTo(128f, 96.89f)
                lineTo(107.11f, 76f)
                horizontalLineTo(122f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -44f)
                horizontalLineTo(54f)
                arcTo(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 32f, 54f)
                verticalLineToRelative(68f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 44f, 0f)
                verticalLineTo(107.11f)
                lineTo(96.89f, 128f)
                lineTo(70.47f, 154.42f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = true, isPositiveArc = false, 31.11f, 31.11f)
                lineTo(128f, 159.11f)
                lineToRelative(7.57f, 7.57f)
                arcTo(149.19f, 149.19f, 0f, isMoreThanHalf = false, isPositiveArc = false, 106f, 256f)
                curveToRelative(0f, 82.71f, 67.29f, 150f, 150f, 150f)
                arcToRelative(149.2f, 149.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 89.46f, -29.67f)
                lineTo(369f, 399.9f)
                lineToRelative(-26.54f, 26.54f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 31.12f, 31.12f)
                lineToRelative(26.49f, -26.5f)
                lineToRelative(42.37f, 42.48f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 31.16f, -31.08f)
                lineTo(431.17f, 400f)
                lineToRelative(26.39f, -26.39f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, -31.12f, -31.12f)
                lineToRelative(-26.35f, 26.35f)
                lineToRelative(-23.55f, -23.62f)
                arcToRelative(149.68f, 149.68f, 0f, isMoreThanHalf = false, isPositiveArc = false, -0.11f, -178.49f)
                lineTo(436f, 107.11f)
                verticalLineTo(122f)
                arcToRelative(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 44f, 0f)
                verticalLineTo(54f)
                arcTo(22f, 22f, 0f, isMoreThanHalf = false, isPositiveArc = false, 458f, 32f)
                close()
                moveTo(150f, 256f)
                arcTo(106f, 106f, 0f, isMoreThanHalf = true, isPositiveArc = true, 256f, 362f)
                arcTo(106.12f, 106.12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 150f, 256f)
                close()
            }
        }.build()

        return _CiTransgender!!
    }

@Suppress("ObjectPropertyName")
private var _CiTransgender: ImageVector? = null
