package com.anytypeio.anytype.feature_vault.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.feature_vault.R

@Composable
fun CreateChannelDropdownMenu(
    expanded: Boolean,
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
        containerColor = colorResource(id = R.color.background_secondary)
    ) {
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = colorResource(id = R.color.palette_system_blue),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.vault_create_personal),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary)
                    )
                }
            },
            onClick = onPersonalClicked
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            color = colorResource(id = R.color.shape_transparent_secondary)
        )
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = colorResource(id = R.color.palette_system_green),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.vault_create_group),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary)
                    )
                }
            },
            onClick = onGroupClicked
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            color = colorResource(id = R.color.shape_transparent_secondary)
        )
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = colorResource(id = R.color.palette_system_amber_125),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.vault_join_via_qr),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary)
                    )
                }
            },
            onClick = onJoinViaQrClicked
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
