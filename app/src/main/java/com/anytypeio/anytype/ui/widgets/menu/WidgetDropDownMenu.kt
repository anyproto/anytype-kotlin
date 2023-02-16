package com.anytypeio.anytype.ui.widgets.menu

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R

@Composable
fun WidgetMenu(
    isExpanded: MutableState<Boolean>,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit
) {
    DropdownMenu(
        expanded = isExpanded.value,
        onDismissRequest = { isExpanded.value = false },
        offset = DpOffset(x = 0.dp, y = 12.dp)
    ) {
        DropdownMenuItem(
            onClick = {
                onDropDownMenuAction(DropDownMenuAction.ChangeWidgetSource).also {
                    isExpanded.value = false
                }
            }
        ) {
            Text(stringResource(R.string.widget_change_source))
        }
        Divider(thickness = 0.5.dp)
        DropdownMenuItem(
            onClick = {
                onDropDownMenuAction(DropDownMenuAction.ChangeWidgetType).also {
                    isExpanded.value = false
                }
            }
        ) {
            Text(stringResource(R.string.widget_change_type))
        }
        Divider(thickness = 0.5.dp)
        DropdownMenuItem(
            onClick = {
                onDropDownMenuAction(DropDownMenuAction.RemoveWidget).also {
                    isExpanded.value = false
                }
            }
        ) {
            Text(stringResource(id = R.string.remove_widget))
        }
    }
}

@Composable
fun WidgetActionButton(
    modifier: Modifier,
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(id = R.color.text_primary),
            contentColor = colorResource(id = R.color.widget_button)
        ),
        modifier = modifier,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = label)
    }
}

sealed class DropDownMenuAction {
    object ChangeWidgetType : DropDownMenuAction()
    object ChangeWidgetSource : DropDownMenuAction()
    object RemoveWidget : DropDownMenuAction()
    object EditWidgets : DropDownMenuAction()
}