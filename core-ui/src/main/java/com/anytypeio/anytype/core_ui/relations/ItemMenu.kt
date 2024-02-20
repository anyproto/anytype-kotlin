package com.anytypeio.anytype.core_ui.relations

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.BodyCallout

@Composable
fun ItemMenu(
    action: (ItemMenuAction) -> Unit,
    isMenuExpanded: MutableState<Boolean>,
    showEdit: Boolean = false,
    showOpen: Boolean = false,
    showDuplicate: Boolean = false,
    showDelete: Boolean = false
) {
    DropdownMenu(
        expanded = isMenuExpanded.value,
        onDismissRequest = { isMenuExpanded.value = false },
        modifier = Modifier.width(220.dp)
    ) {
        if (showOpen) {
            DropdownMenuItem(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
                onClick = {
                    isMenuExpanded.value = false
                    action(ItemMenuAction.Open)
                },
            ) {
                Text(
                    text = stringResource(R.string.open_object),
                    style = BodyCallout,
                    color = colorResource(id = R.color.text_primary),
                )
            }
            Divider(paddingEnd = 0.dp, paddingStart = 0.dp)
        }
        if (showEdit) {
            DropdownMenuItem(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
                onClick = {
                    isMenuExpanded.value = false
                    action(ItemMenuAction.Edit)
                },
            ) {
                Text(
                    text = stringResource(R.string.edit),
                    style = BodyCallout,
                    color = colorResource(id = R.color.text_primary),
                )
            }
            Divider(paddingEnd = 0.dp, paddingStart = 0.dp)
        }
        if (showDuplicate) {
            DropdownMenuItem(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
                onClick = {
                    isMenuExpanded.value = false
                    action(ItemMenuAction.Duplicate)
                }
            ) {
                Text(
                    text = stringResource(R.string.duplicate),
                    style = BodyCallout,
                    color = colorResource(id = R.color.text_primary),
                )
            }
            Divider(paddingEnd = 0.dp, paddingStart = 0.dp)
        }
        if (showDelete) {
            DropdownMenuItem(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
                onClick = {
                    isMenuExpanded.value = false
                    action(ItemMenuAction.Delete)
                },
            ) {
                Text(
                    text = stringResource(R.string.delete),
                    style = BodyCallout,
                    color = colorResource(id = R.color.palette_system_red),
                )
            }
        }
    }
}

sealed class ItemMenuAction {
    object Open : ItemMenuAction()
    object Edit : ItemMenuAction()
    object Duplicate : ItemMenuAction()
    object Delete : ItemMenuAction()
}