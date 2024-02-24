package com.anytypeio.anytype.core_ui.features.multiplayer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anytypeio.anytype.presentation.multiplayer.ShareSpaceViewModel

@Composable
fun ShareSpaceScreen(
    viewState: ShareSpaceViewModel.ViewState,
    onRegenerateInviteLinkClicked: () -> Unit,
    onShareInviteLinkClicked: () -> Unit
) {
    Box(modifier = Modifier.wrapContentSize()) {
        when(viewState) {
            ShareSpaceViewModel.ViewState.Init -> {
                // Do nothing.
            }
            is ShareSpaceViewModel.ViewState.Share -> {
                ShareInviteLinkCard(
                    link = viewState.link,
                    onShareInviteClicked = onShareInviteLinkClicked,
                    onRegenerateInviteLinkClicked = onRegenerateInviteLinkClicked
                )
            }
        }
    }
}