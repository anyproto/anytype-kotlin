package com.anytypeio.anytype.core_ui.features.fields

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.R

@Composable
fun ItemDropDownMenu(
    showMenu: Boolean,
    onDismissRequest: () -> Unit,
    onAddToCurrentTypeClick: () -> Unit,
    onRemoveFromObjectClick: () -> Unit,
) {
    DropdownMenu(
        modifier = Modifier
            .width(244.dp),
        expanded = showMenu,
        offset = DpOffset(x = 0.dp, y = 0.dp),
        onDismissRequest = {
            onDismissRequest()
        },
        shape = RoundedCornerShape(10.dp),
        containerColor = colorResource(id = R.color.background_secondary),
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(R.string.field_menu_add_to_type),
                    style = BodyCalloutRegular,
                    color = colorResource(id = R.color.text_primary)
                )
            },
            onClick = {
                onAddToCurrentTypeClick()
            },
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(R.string.field_menu_remove_from_object),
                    style = BodyCalloutRegular,
                    color = colorResource(id = R.color.palette_system_red)
                )
            },
            onClick = {
                onRemoveFromObjectClick()
            },
        )
    }
}