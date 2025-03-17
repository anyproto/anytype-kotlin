package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val CustomIcons.CiWarning: ImageVector
    get() {
        if (_CiWarning != null) {
            return _CiWarning!!
        }
        _CiWarning = ImageVector.Builder(
            name = "CiWarning",
            defaultWidth = 512.dp,
            defaultHeight = 512.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(449.07f, 399.08f)
                lineTo(278.64f, 82.58f)
                curveToRelative(-12.08f, -22.44f, -44.26f, -22.44f, -56.35f, 0f)
                lineTo(51.87f, 399.08f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 80f, 446.25f)
                lineTo(420.89f, 446.25f)
                arcTo(32f, 32f, 0f, isMoreThanHalf = false, isPositiveArc = false, 449.07f, 399.08f)
                close()
                moveTo(250.47f, 397.25f)
                arcToRelative(20f, 20f, 0f, isMoreThanHalf = true, isPositiveArc = true, 20f, -20f)
                arcTo(20f, 20f, 0f, isMoreThanHalf = false, isPositiveArc = true, 250.47f, 397.25f)
                close()
                moveTo(272.19f, 196.1f)
                lineToRelative(-5.74f, 122f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = false, isPositiveArc = true, -32f, 0f)
                lineToRelative(-5.74f, -121.95f)
                verticalLineToRelative(0f)
                arcToRelative(21.73f, 21.73f, 0f, isMoreThanHalf = false, isPositiveArc = true, 21.5f, -22.69f)
                horizontalLineToRelative(0.21f)
                arcToRelative(21.74f, 21.74f, 0f, isMoreThanHalf = false, isPositiveArc = true, 21.73f, 22.7f)
                close()
            }
        }.build()

        return _CiWarning!!
    }

@Suppress("ObjectPropertyName")
private var _CiWarning: ImageVector? = null
