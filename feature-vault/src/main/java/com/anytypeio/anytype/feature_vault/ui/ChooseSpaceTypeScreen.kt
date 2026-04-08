package com.anytypeio.anytype.feature_vault.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.R as CoreR

@Composable
fun CreateChannelDropdownMenu(
    expanded: Boolean,
    isLocalOnly: Boolean = false,
    onPersonalClicked: () -> Unit,
    onGroupClicked: () -> Unit,
    onJoinViaQrClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        modifier = Modifier.width(254.dp),
        expanded = expanded,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(12.dp),
        containerColor = colorResource(id = CoreR.color.background_secondary),
        tonalElevation = 8.dp,
        offset = DpOffset(
            x = 16.dp,
            y = 8.dp
        )
    ) {
        DropdownMenuItem(
            text = {
                CreateChannelMenuItemContent(
                    icon = CoreR.drawable.ci_person,
                    text = stringResource(id = com.anytypeio.anytype.localization.R.string.vault_create_personal)
                )
            },
            onClick = {
                onPersonalClicked()
                onDismiss()
            }
        )
        if (!isLocalOnly) {
            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
            DropdownMenuItem(
                text = {
                    CreateChannelMenuItemContent(
                        icon = CoreR.drawable.ci_people,
                        text = stringResource(id = com.anytypeio.anytype.localization.R.string.vault_create_group)
                    )
                },
                onClick = {
                    onGroupClicked()
                    onDismiss()
                }
            )
            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
            DropdownMenuItem(
                text = {
                    CreateChannelMenuItemContent(
                        icon = CoreR.drawable.ic_join_via_qr_code_32,
                        text = stringResource(id = com.anytypeio.anytype.localization.R.string.vault_join_via_qr)
                    )
                },
                onClick = {
                    onJoinViaQrClicked()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun CreateChannelMenuItemContent(
    icon: Int,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = BodyRegular,
            color = colorResource(CoreR.color.text_primary),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Image(
            painter = painterResource(id = icon),
            contentDescription = text,
            colorFilter = ColorFilter.tint(colorResource(CoreR.color.text_primary)),
            modifier = Modifier.padding(end = 16.dp).size(24.dp)
        )
    }
}

@DefaultPreviews
@Composable
private fun CreateChannelDropdownMenuPreview() {
    CreateChannelDropdownMenu(
        expanded = true,
        onPersonalClicked = {},
        onGroupClicked = {},
        onJoinViaQrClicked = {},
        onDismiss = {}
    )
}
