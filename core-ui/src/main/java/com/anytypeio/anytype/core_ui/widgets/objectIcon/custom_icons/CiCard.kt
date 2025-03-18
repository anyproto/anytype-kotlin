package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val CustomIcons.CiCard: ImageVector
    get() {
        if (_CiCard != null) {
            return _CiCard!!
        }
        _CiCard = ImageVector.Builder(
            name = "CiCard",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(32f, 376f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = false, 56f, 56f)
                lineTo(424f, 432f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = false, 56f, -56f)
                lineTo(480f, 222f)
                lineTo(32f, 222f)
                close()
                moveTo(98f, 300f)
                arcToRelative(30f, 30f, 0f, isMoreThanHalf = false, isPositiveArc = true, 30f, -30f)
                horizontalLineToRelative(48f)
                arcToRelative(30f, 30f, 0f, isMoreThanHalf = false, isPositiveArc = true, 30f, 30f)
                verticalLineToRelative(20f)
                arcToRelative(30f, 30f, 0f, isMoreThanHalf = false, isPositiveArc = true, -30f, 30f)
                lineTo(128f, 350f)
                arcToRelative(30f, 30f, 0f, isMoreThanHalf = false, isPositiveArc = true, -30f, -30f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(424f, 80f)
                horizontalLineTo(88f)
                arcToRelative(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = false, -56f, 56f)
                verticalLineToRelative(26f)
                horizontalLineTo(480f)
                verticalLineTo(136f)
                arcTo(56f, 56f, 0f, isMoreThanHalf = false, isPositiveArc = false, 424f, 80f)
                close()
            }
        }.build()

        return _CiCard!!
    }

@Suppress("ObjectPropertyName")
private var _CiCard: ImageVector? = null
