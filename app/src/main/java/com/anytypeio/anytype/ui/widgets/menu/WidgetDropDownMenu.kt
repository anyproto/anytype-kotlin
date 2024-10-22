package com.anytypeio.anytype.ui.widgets.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction

@Composable
fun WidgetMenu(
    canRemove: Boolean = true,
    canChangeSource: Boolean = true,
    canChangeType: Boolean = true,
    canEmptyBin: Boolean = false,
    canEditWidgets: Boolean = true,
    canAddBelow: Boolean = true,
    isExpanded: MutableState<Boolean>,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit
) {
    DropdownMenu(
        expanded = isExpanded.value,
        onDismissRequest = { isExpanded.value = false },
        offset = DpOffset(x = 0.dp, y = 6.dp)
    ) {
        val extraEndPadding = 68.dp
        val defaultTextStyle = BodyRegular
        if (canAddBelow) {
            DropdownMenuItem(
                onClick = {
                    onDropDownMenuAction(DropDownMenuAction.AddBelow).also {
                        isExpanded.value = false
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.widget_add_below),
                    style = BodyRegular,
                    color = colorResource(id = R.color.text_primary),
                    modifier = Modifier.padding(end = extraEndPadding)
                )
            }
            Divider(
                thickness = 0.5.dp,
                color = colorResource(id = R.color.shape_primary)
            )
        }
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
                    style = defaultTextStyle,
                    color = colorResource(id = R.color.text_primary),
                    modifier = Modifier.padding(end = extraEndPadding)
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
                    style = defaultTextStyle,
                    color = colorResource(id = R.color.text_primary),
                    modifier = Modifier.padding(end = extraEndPadding)
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
                    text = stringResource(id = R.string.widget_remove_widget),
                    style = defaultTextStyle.copy(
                        color = colorResource(id = R.color.palette_dark_red)
                    ),
                    modifier = Modifier.padding(end = extraEndPadding)
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
                    text = stringResource(id = R.string.widget_empty_bin),
                    style = defaultTextStyle.copy(
                        color = colorResource(id = R.color.palette_dark_red)
                    ),
                    modifier = Modifier.padding(end = extraEndPadding)
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
                    text = stringResource(R.string.widget_edit_widgets),
                    style = defaultTextStyle,
                    color = colorResource(id = R.color.text_primary),
                    modifier = Modifier.padding(end = extraEndPadding)
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
            .defaultMinSize(minHeight = 32.dp)
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
                .padding(horizontal = 12.dp, vertical = 6.dp),
            text = label,
            style = Title2,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}