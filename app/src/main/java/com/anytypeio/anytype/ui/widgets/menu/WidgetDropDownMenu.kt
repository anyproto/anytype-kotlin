package com.anytypeio.anytype.ui.widgets.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable

@Composable
fun WidgetMenu(
    isExpanded: MutableState<Boolean>,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit
) {
    DropdownMenu(
        expanded = isExpanded.value,
        onDismissRequest = { isExpanded.value = false },
        offset = DpOffset(x = 0.dp, y = 6.dp)
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
            Text(
                text = stringResource(id = R.string.remove_widget),
                color = colorResource(id = R.color.palette_dark_red)
            )
        }
        Divider(thickness = 0.5.dp)
        DropdownMenuItem(
            onClick = {
                onDropDownMenuAction(DropDownMenuAction.EditWidgets).also {
                    isExpanded.value = false
                }
            }
        ) {
            Text(stringResource(R.string.edit_widgets))
        }
    }
}


@Composable
fun WidgetActionButton(
    modifier: Modifier,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(32.dp)
            .background(
                shape = RoundedCornerShape(8.dp),
                color = colorResource(id = R.color.widget_button)
            )
            .noRippleClickable { onClick() }
        ,
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 12.dp),
            text = label,
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = colorResource(id = R.color.text_primary)
            )
        )
    }
}

sealed class DropDownMenuAction {
    object ChangeWidgetType : DropDownMenuAction()
    object ChangeWidgetSource : DropDownMenuAction()
    object RemoveWidget : DropDownMenuAction()
    object EditWidgets : DropDownMenuAction()
}