package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.feature_chats.R

@Composable
fun ChatMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onPropertiesClick: () -> Unit,
    onEditInfoClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onPinClick: () -> Unit,
    onMoveToBinClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        DropdownMenu(
            modifier = Modifier.align(Alignment.TopEnd),
            offset = DpOffset((-16).dp, 8.dp),
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = false),
            shape = RoundedCornerShape(10.dp),
            containerColor = colorResource(id = R.color.background_secondary),
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.properties),
                        color = colorResource(id = R.color.text_primary),
                        modifier = Modifier.padding(end = 64.dp)
                    )
                },
                onClick = {
                    onPropertiesClick()
                }
            )
            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.chat_edit_info),
                        color = colorResource(id = R.color.text_primary),
                        modifier = Modifier.padding(end = 64.dp)
                    )
                },
                onClick = {
                    onEditInfoClick()
                }
            )
            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.notifications_title),
                        color = colorResource(id = R.color.text_primary),
                        modifier = Modifier.padding(end = 64.dp)
                    )
                },
                onClick = {
                    onNotificationsClick()
                }
            )
            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.object_action_pin),
                        color = colorResource(id = R.color.text_primary),
                        modifier = Modifier.padding(end = 64.dp)
                    )
                },
                onClick = {
                    onPinClick()
                }
            )
            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.chat_move_to_bin),
                        color = colorResource(id = R.color.palette_system_red),
                        modifier = Modifier.padding(end = 64.dp)
                    )
                },
                onClick = {
                    onMoveToBinClick()
                }
            )
        }
    }
}

@DefaultPreviews
@Composable
fun ChatMenuPreview() {
    ChatMenu(
        expanded = true,
        onDismissRequest = {},
        onPropertiesClick = {},
        onEditInfoClick = {},
        onNotificationsClick = {},
        onPinClick = {},
        onMoveToBinClick = {}
    )
}