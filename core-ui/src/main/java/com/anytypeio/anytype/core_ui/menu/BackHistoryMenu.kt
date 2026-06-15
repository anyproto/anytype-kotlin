package com.anytypeio.anytype.core_ui.menu

import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.presentation.navigation.backstack.BackHistoryMenuItem
import com.anytypeio.anytype.presentation.navigation.backstack.BackHistoryMenuState

/**
 * Long-press-on-back-button history menu (DROID-4518).
 * Shows "Channels" (exit to vault), an optional "Home" entry when the space home (widgets)
 * screen is in the back stack, then up to 5 recently visited objects.
 * Renders nothing while [state] is [BackHistoryMenuState.Hidden].
 */
@Composable
fun BackHistoryMenu(
    state: BackHistoryMenuState,
    onChannelsClicked: () -> Unit,
    onItemClicked: (BackHistoryMenuItem) -> Unit,
    onDismiss: () -> Unit
) {
    val visible = state as? BackHistoryMenuState.Visible
    val items = visible?.items.orEmpty()
    val homeEntryId = visible?.homeEntryId
    StyledDropdownMenu(
        expanded = state is BackHistoryMenuState.Visible,
        onDismissRequest = onDismiss,
        offset = DpOffset(x = 0.dp, y = 8.dp),
        width = 244.dp
    ) {
        DropdownMenuItem(
            modifier = Modifier.height(44.dp),
            onClick = onChannelsClicked,
            text = {
                Text(
                    text = stringResource(R.string.back_history_channels),
                    style = BodyRegular,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
        if (homeEntryId != null) {
            Divider(
                height = 0.5.dp,
                paddingStart = 0.dp,
                paddingEnd = 0.dp,
                color = colorResource(R.color.shape_primary)
            )
            DropdownMenuItem(
                modifier = Modifier.height(44.dp),
                onClick = {
                    onItemClicked(
                        BackHistoryMenuItem(
                            entryId = homeEntryId,
                            objectId = "",
                            space = "",
                            name = ""
                        )
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.space_home_row_title),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
        items.forEach { item ->
            Divider(
                height = 0.5.dp,
                paddingStart = 0.dp,
                paddingEnd = 0.dp,
                color = colorResource(R.color.shape_primary)
            )
            DropdownMenuItem(
                modifier = Modifier.height(44.dp),
                onClick = { onItemClicked(item) },
                text = {
                    Text(
                        text = item.name.ifBlank { stringResource(R.string.untitled) },
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}
