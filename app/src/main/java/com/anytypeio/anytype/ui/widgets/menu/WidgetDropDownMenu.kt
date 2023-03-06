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
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction

@Composable
fun WidgetMenu(
    canRemove: Boolean = true,
    canChangeSource: Boolean = true,
    canChangeType: Boolean = true,
    canEmptyBin: Boolean = false,
    canEditWidgets: Boolean = true,
    isExpanded: MutableState<Boolean>,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit
) {
    DropdownMenu(
        expanded = isExpanded.value,
        onDismissRequest = { isExpanded.value = false },
        offset = DpOffset(x = 0.dp, y = 6.dp)
    ) {
        val defaultTextStyle = TextStyle(
            color = colorResource(id = R.color.text_primary),
            fontSize = 17.sp
        )
        if (canChangeSource) {
            DropdownMenuItem(
                onClick = {
                    onDropDownMenuAction(DropDownMenuAction.ChangeWidgetSource).also {
                        isExpanded.value = false
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.widget_change_source),
                    style = defaultTextStyle
                )
            }
            Divider(
                thickness = 0.5.dp,
                color = colorResource(id = R.color.shape_primary)
            )
        }
        if (canChangeType) {
            DropdownMenuItem(
                onClick = {
                    onDropDownMenuAction(DropDownMenuAction.ChangeWidgetType).also {
                        isExpanded.value = false
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.widget_change_type),
                    style = defaultTextStyle
                )
            }
            Divider(
                thickness = 0.5.dp,
                color = colorResource(id = R.color.shape_primary)
            )
        }
        if (canRemove) {
            DropdownMenuItem(
                onClick = {
                    onDropDownMenuAction(DropDownMenuAction.RemoveWidget).also {
                        isExpanded.value = false
                    }
                }
            ) {
                Text(
                    text = stringResource(id = R.string.remove_widget),
                    style = TextStyle(
                        color = colorResource(id = R.color.palette_dark_red),
                        fontSize = 17.sp
                    )
                )
            }
            Divider(
                thickness = 0.5.dp,
                color = colorResource(id = R.color.shape_primary)
            )
        }
        if (canEmptyBin) {
            DropdownMenuItem(
                onClick = {
                    onDropDownMenuAction(DropDownMenuAction.EmptyBin).also {
                        isExpanded.value = false
                    }
                }
            ) {
                Text(
                    text = stringResource(id = R.string.empty_bin),
                    style = TextStyle(
                        color = colorResource(id = R.color.palette_dark_red),
                        fontSize = 17.sp
                    )
                )
            }
            Divider(
                thickness = 0.5.dp,
                color = colorResource(id = R.color.shape_primary)
            )
        }
        if (canEditWidgets) {
            DropdownMenuItem(
                onClick = {
                    onDropDownMenuAction(DropDownMenuAction.EditWidgets).also {
                        isExpanded.value = false
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.edit_widgets),
                    style = defaultTextStyle
                )
            }
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
                color = colorResource(id = R.color.background_primary).copy(alpha = 0.65f)
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