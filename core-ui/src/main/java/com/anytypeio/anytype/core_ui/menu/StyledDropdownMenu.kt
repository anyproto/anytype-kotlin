package com.anytypeio.anytype.core_ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.anytypeio.anytype.core_ui.R

/**
 * Default styling constants for styled dropdown menus.
 */
object StyledDropdownMenuDefaults {
    val Width = 280.dp
    val CornerRadius = 12.dp
    val DefaultOffset = DpOffset(8.dp, 0.dp)
}

/**
 * A styled dropdown menu with consistent appearance across the app.
 * Uses secondary background color and rounded corners.
 *
 * @param expanded Whether the menu is currently visible
 * @param onDismissRequest Callback when the menu should be dismissed
 * @param modifier Optional modifier for additional customization
 * @param offset Offset for positioning the menu relative to its anchor
 * @param width Width of the menu
 * @param cornerRadius Corner radius for the menu shape
 * @param focusable Whether the menu should request focus
 * @param content The content to display inside the menu
 */
@Composable
fun StyledDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = StyledDropdownMenuDefaults.DefaultOffset,
    width: Dp = StyledDropdownMenuDefaults.Width,
    cornerRadius: Dp = StyledDropdownMenuDefaults.CornerRadius,
    focusable: Boolean = false,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val backgroundColor = colorResource(id = R.color.background_secondary)

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .width(width)
            .background(
                color = backgroundColor,
                shape = shape
            ),
        offset = offset,
        shape = shape,
        containerColor = backgroundColor,
        properties = PopupProperties(focusable = focusable)
    ) {
        content()
    }
}
