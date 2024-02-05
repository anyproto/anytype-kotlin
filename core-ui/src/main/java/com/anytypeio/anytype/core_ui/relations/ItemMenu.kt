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
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationsListItem
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagStatusAction

@Composable
fun ItemMenu(
    item: RelationsListItem.Item?,
    action: (TagStatusAction) -> Unit,
    isMenuExpanded: MutableState<Boolean>
) {
    if (item != null) {
        DropdownMenu(
            expanded = isMenuExpanded.value,
            onDismissRequest = { isMenuExpanded.value = false },
            modifier = Modifier.width(220.dp)
        ) {
            DropdownMenuItem(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
                onClick = {
                    isMenuExpanded.value = false
                    action(TagStatusAction.Edit(item.optionId))
                },
            ) {
                Text(
                    text = stringResource(R.string.edit),
                    style = BodyCallout,
                    color = colorResource(id = R.color.text_primary),
                )
            }
            Divider(paddingEnd = 0.dp, paddingStart = 0.dp)
            DropdownMenuItem(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
                onClick = {
                    isMenuExpanded.value = false
                    action(TagStatusAction.Duplicate(item.optionId))
                }
            ) {
                Text(
                    text = stringResource(R.string.duplicate),
                    style = BodyCallout,
                    color = colorResource(id = R.color.text_primary),
                )
            }
            Divider(paddingEnd = 0.dp, paddingStart = 0.dp)
            DropdownMenuItem(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
                onClick = {
                    isMenuExpanded.value = false
                    action(TagStatusAction.Delete(item.optionId))
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